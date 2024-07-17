package de.hbch.traewelling.ui.report

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.report.Report
import de.hbch.traewelling.logging.Logger

class ReportViewModel : ViewModel() {
    suspend fun createReport(report: Report): Boolean {
        try {
            val response = TraewellingApi.reportService.createReport(report)
            return response.isSuccessful
        } catch (ex: Exception) {
            Logger.captureException(ex)
            return false
        }
    }
}
