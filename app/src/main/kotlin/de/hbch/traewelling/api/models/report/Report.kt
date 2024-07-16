package de.hbch.traewelling.api.models.report

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class Report(
    @SerializedName("subject_type") val subjectType: ReportSubjectType,
    @SerializedName("subject_id") val subjectId: Int,
    val reason: ReportReason,
    val description: String?
)

enum class ReportSubjectType {
    @SerializedName("Event") EVENT,
    @SerializedName("Status") STATUS,
    @SerializedName("User") USER
}

enum class ReportReason {
    @SerializedName("inappropriate")
    INAPPROPRIATE {
        override val title = R.string.report_reason_inappropriate
        override val icon = R.drawable.ic_inappropriate
    },
    @SerializedName("implausible")
    IMPLAUSIBLE {
        override val title = R.string.report_reason_implausible
        override val icon = R.drawable.ic_error
    },
    @SerializedName("spam")
    SPAM {
        override val title = R.string.report_reason_spam
        override val icon = R.drawable.ic_announcement
    },
    @SerializedName("illegal")
    ILLEGAL {
        override val title = R.string.report_reason_illegal
        override val icon = R.drawable.ic_police
    },
    @SerializedName("other")
    OTHER {
        override val title = R.string.report_reason_other
        override val icon = R.drawable.ic_unknown
    };

    abstract val title: Int
    abstract val icon: Int
}
