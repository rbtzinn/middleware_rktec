import { onObjectFinalized } from "firebase-functions/v2/storage"; // MUDANÇA 1: Importamos a função específica da v2
import { logger } from "firebase-functions"; // MUDANÇA 2: O logger é importado assim agora
import * as admin from "firebase-admin";
import * as xlsx from "xlsx";
import * as os from "os";
import * as path from "path";
import * as fs from "fs";

admin.initializeApp();

const db = admin.firestore();

// Interface para garantir a tipagem do nosso objeto de mapeamento
interface Mapeamento {
  colunaEpc: number;
  colunaNome?: number;
  colunaSetor?: number;
  colunaLoja?: number;
}

// MUDANÇA 3: A estrutura da função é diferente na v2
export const processarPlanilhadeinventario = onObjectFinalized(
  {
    bucket: "rktec-3b6ba.appspot.com", // IMPORTANTE: Coloque o nome do seu bucket aqui! Você encontra na página principal do Storage no Console do Firebase.
    region: "southamerica-east1",
    timeoutSeconds: 540,
    memory: "1GiB",
  },
  async (event) => {
    // MUDANÇA 4: O objeto 'event' é diferente. Pegamos os dados de 'event.data'
    const filePath = event.data.name;
    const fileBucket = event.data.bucket;
    const bucket = admin.storage().bucket(fileBucket);

    if (!filePath || !filePath.startsWith("imports/")) {
      logger.log("Não é um arquivo de importação. Ignorando.", { filePath });
      return;
    }

    const parts = filePath.split("/");
    const companyId = parts[1];

    if (!companyId) {
      logger.error("Não foi possível extrair o companyId do caminho.", { filePath });
      return;
    }

    logger.log(`Iniciando processamento para empresa: ${companyId}`);

    const mapeamentoRef = db.collection("empresas").doc(companyId).collection("config").doc("mapeamento");
    const mapeamentoDoc = await mapeamentoRef.get();

    if (!mapeamentoDoc.exists) {
      logger.error(`Configuração de mapeamento não encontrada para a empresa ${companyId}. Abortando.`);
      return;
    }
    const mapeamento = mapeamentoDoc.data() as Mapeamento;
    logger.log("Configuração de mapeamento encontrada:", mapeamento);

    const tempFilePath = path.join(os.tmpdir(), path.basename(filePath));

    try {
      await bucket.file(filePath).download({ destination: tempFilePath });
      logger.log(`Arquivo baixado para: ${tempFilePath}`);

      const workbook = xlsx.readFile(tempFilePath, { cellDates: true });
      const sheetName = workbook.SheetNames[0];
      const worksheet = workbook.Sheets[sheetName];
      const data: any[][] = xlsx.utils.sheet_to_json(worksheet, { header: 1 });

      if (data.length <= 1) { // Verifica se há dados além do cabeçalho
        throw new Error("A planilha está vazia ou contém apenas o cabeçalho.");
      }

      const dataRows: any[][] = data.slice(1);
      logger.log(`Encontrados ${dataRows.length} registros na planilha.`);

      const inventarioRef = db.collection("inventario");
      const snapshot = await inventarioRef.where("companyId", "==", companyId).get();
      if (!snapshot.empty) {
        const deleteBatch = db.batch();
        snapshot.docs.forEach((doc) => deleteBatch.delete(doc.ref));
        await deleteBatch.commit();
        logger.log(`Inventário antigo da empresa ${companyId} foi limpo.`);
      }

      const batchArray: admin.firestore.WriteBatch[] = [];
      batchArray.push(db.batch());
      let operationCounter = 0;
      let batchIndex = 0;

      for (const row of dataRows) {
        const tag = row[mapeamento.colunaEpc];
        if (tag === undefined || tag === null || String(tag).trim() === "") continue;

        const newItem = {
          tag: String(tag),
          desc: mapeamento.colunaNome !== undefined ? String(row[mapeamento.colunaNome] ?? "") : "",
          localizacao: mapeamento.colunaSetor !== undefined ? String(row[mapeamento.colunaSetor] ?? "") : "",
          loja: mapeamento.colunaLoja !== undefined ? String(row[mapeamento.colunaLoja] ?? "") : "",
          companyId: companyId,
          colunasExtras: {},
        };

        const newDocRef = inventarioRef.doc();
        batchArray[batchIndex].set(newDocRef, newItem);
        operationCounter++;

        if (operationCounter === 499) {
          batchArray.push(db.batch());
          batchIndex++;
          operationCounter = 0;
        }
      }

      await Promise.all(batchArray.map((batch) => batch.commit()));
      logger.log(`${dataRows.length} novos itens adicionados ao Firestore.`);

      await db.collection("empresas").doc(companyId).update({
        planilhaImportada: true,
      });
      logger.log(`Empresa ${companyId} marcada como configurada com sucesso!`);

    } catch (error) {
      logger.error("Erro CRÍTICO ao processar a planilha:", error);
    } finally {
      fs.unlinkSync(tempFilePath);
    }
  }
);