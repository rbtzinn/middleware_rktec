import { onObjectFinalized } from "firebase-functions/v2/storage";
import { logger } from "firebase-functions";
import * as admin from "firebase-admin";
import * as xlsx from "xlsx";
import * as os from "os";
import * as path from "path";
import * as fs from "fs";

admin.initializeApp();
const db = admin.firestore();

interface Mapeamento {
  colunaEpc: number;
  colunaNome?: number;
  colunaSetor?: number;
  colunaLoja?: number;
}

export const processarPlanilhadeinventario = onObjectFinalized(
  {
    bucket: "rktec-app-novo.firebasestorage.app", // Verifique se este é o nome correto do seu bucket
    region: "southamerica-east1",
    timeoutSeconds: 540,
    memory: "1GiB",
  },
  async (event) => {
    const filePath = event.data.name;
    const fileBucket = event.data.bucket;
    const bucket = admin.storage().bucket(fileBucket);

    if (!filePath || !filePath.startsWith("imports/")) return;
    const parts = filePath.split("/");
    const companyId = parts[1];
    if (!companyId) return;

    const empresaRef = db.collection("empresas").doc(companyId);
    let tempFilePath = "";

    try {
      await empresaRef.update({ statusProcessamento: "INICIANDO" });

      const mapeamentoRef = empresaRef.collection("config").doc("mapeamento");
      const mapeamentoDoc = await mapeamentoRef.get();
      if (!mapeamentoDoc.exists) throw new Error("Mapeamento não encontrado.");

      const mapeamento = mapeamentoDoc.data() as Mapeamento;
      const indicesMapeados = Object.values(mapeamento); // Pega os índices já usados [0, 2, 3, 4]

      tempFilePath = path.join(os.tmpdir(), path.basename(filePath));
      await bucket.file(filePath).download({ destination: tempFilePath });

      const workbook = xlsx.readFile(tempFilePath, { cellDates: true });
      const sheetName = workbook.SheetNames[0];
      const worksheet = workbook.Sheets[sheetName];
      const data: any[][] = xlsx.utils.sheet_to_json(worksheet, { header: 1 });

      if (data.length <= 1) throw new Error("A planilha está vazia.");

      const headerRow = data[0]; // Pega a linha do cabeçalho
      const dataRows = data.slice(1);

      const inventarioJson = dataRows.map((row, index) => {
        // ##### LÓGICA ATUALIZADA PARA COLUNAS EXTRAS #####
        const colunasExtras: { [key: string]: string } = {};

        // Itera por TODAS as colunas da linha atual
        headerRow.forEach((columnName, columnIndex) => {
          // Se o índice da coluna NÃO estiver na lista de colunas já mapeadas
          if (!indicesMapeados.includes(columnIndex)) {
            // Adiciona ao objeto de colunas extras
            colunasExtras[String(columnName)] = String(row[columnIndex] ?? "");
          }
        });

        return {
          tag: String(row[mapeamento.colunaEpc] ?? ""),
          desc: mapeamento.colunaNome !== undefined ? String(row[mapeamento.colunaNome] ?? "") : "",
          localizacao: mapeamento.colunaSetor !== undefined ? String(row[mapeamento.colunaSetor] ?? "") : "",
          loja: mapeamento.colunaLoja !== undefined ? String(row[mapeamento.colunaLoja] ?? "") : "",
          companyId: companyId,
          originalRow: index + 2,
          colunasExtras: colunasExtras, // Salva o objeto preenchido
        };
      });

      const jsonString = JSON.stringify(inventarioJson);
      const tempJsonPath = path.join(os.tmpdir(), "inventario.json");
      fs.writeFileSync(tempJsonPath, jsonString);

      const jsonDestinationPath = `processed/${companyId}/inventario.json`;
      await bucket.upload(tempJsonPath, {
        destination: jsonDestinationPath,
        metadata: { contentType: "application/json" },
      });

      await empresaRef.update({
        planilhaImportada: true,
        statusProcessamento: "CONCLUIDO",
        inventarioJsonPath: jsonDestinationPath,
      });
      logger.log(`Empresa ${companyId} processada com sucesso.`);

    } catch (error: any) {
      logger.error("Erro CRÍTICO:", error);
      await empresaRef.update({ statusProcessamento: `ERRO: ${error.message}` });
    } finally {
      if (tempFilePath && fs.existsSync(tempFilePath)) fs.unlinkSync(tempFilePath);
    }
  }
);