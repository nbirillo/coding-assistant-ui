package org.jetbrains.research.ml.codingAssistant.hint

import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.hint.HintFactoryImpl
import org.jetbrains.research.ml.coding.assistant.hint.HintManager
import org.jetbrains.research.ml.coding.assistant.hint.HintManagerImpl
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceFileRepository
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.NaiveHintVertexCalculator
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import java.io.File

object CodingAssistantManager : HintManager {
    override fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {
        return manager.getHintedFile(psiFragment, metaInfo)
    }

    private val MOCK_FILE = File(
        "/Users/artembobrov/Documents/masters/ast-transform/coding-assistant/output/max_digit_solution_space.json"
    )

    private val manager = HintManagerImpl(
        HintFactoryImpl(
            SolutionSpaceFileRepository(mapOf(DatasetTask.MAX_DIGIT to MOCK_FILE)),
            ParallelVertexFinder(EditPartialSolutionMatcher),
            NaiveHintVertexCalculator
        )
    )
}
