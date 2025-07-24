package com.example.rktec_middleware.data.model

import java.io.File

sealed class ExportProgress {
    object Idle : ExportProgress()

    data class InProgress(val percent: Int) : ExportProgress()

    data class Success(val file: File) : ExportProgress()

    data class Error(val message: String) : ExportProgress()
}