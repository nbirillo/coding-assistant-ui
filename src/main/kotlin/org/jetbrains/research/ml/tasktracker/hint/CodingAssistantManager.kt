package org.jetbrains.research.ml.tasktracker.hint

import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.hint.HintFactoryImpl
import org.jetbrains.research.ml.coding.assistant.hint.HintManager
import org.jetbrains.research.ml.coding.assistant.hint.HintManagerImpl
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceCachedRepository
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceDirectoryRepository
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.PoissonPathHintVertexCalculator
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import java.io.File

object CodingAssistantManager : HintManager {
    override fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {
        return manager.getHintedFile(psiFragment, metaInfo)
    }

    private val repository = SolutionSpaceCachedRepository(
        SolutionSpaceDirectoryRepository(
            File("/Users/artembobrov/Documents/masters/ast-transform/coding-assistant/output")
        )
    )

    private val manager = HintManagerImpl(
        HintFactoryImpl(
            repository,
            ParallelVertexFinder(EditPartialSolutionMatcher),
            PoissonPathHintVertexCalculator
        )
    )
}
