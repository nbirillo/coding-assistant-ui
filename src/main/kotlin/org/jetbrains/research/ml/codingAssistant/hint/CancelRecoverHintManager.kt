package org.jetbrains.research.ml.codingAssistant.hint

import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo.ProgramExperience.*
import org.jetbrains.research.ml.coding.assistant.hint.CodeHint
import org.jetbrains.research.ml.coding.assistant.hint.HintManager

class CancelRecoverHintManager(private val innerManager: HintManager) : HintManager {
    private var latestFragment: String? = null
    override fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): CodeHint? {
        if (latestFragment != psiFragment.text) {
            return getHintedFileAndUpdateState(psiFragment, metaInfo)
        }
        val higherExperience = metaInfo.programExperience.higherExperience
        if (higherExperience != null) {
            return getHintedFileAndUpdateState(psiFragment, metaInfo.copy(programExperience = higherExperience))
        }
        return getHintedFileAndUpdateState(psiFragment, metaInfo)
    }

    private fun getHintedFileAndUpdateState(psiFragment: PsiFile, metaInfo: MetaInfo): CodeHint? {
        latestFragment = psiFragment.text
        return innerManager.getHintedFile(psiFragment, metaInfo)
    }
}

private val MetaInfo.ProgramExperience.higherExperience: MetaInfo.ProgramExperience?
    get() = when (this) {
        LESS_THAN_HALF_YEAR -> FROM_HALF_TO_ONE_YEAR
        FROM_HALF_TO_ONE_YEAR -> FROM_ONE_TO_TWO_YEARS
        FROM_ONE_TO_TWO_YEARS -> FROM_TWO_TO_FOUR_YEARS
        FROM_TWO_TO_FOUR_YEARS -> FROM_FOUR_TO_SIX_YEARS
        FROM_FOUR_TO_SIX_YEARS -> MORE_THAN_SIX
        MORE_THAN_SIX -> null
    }
