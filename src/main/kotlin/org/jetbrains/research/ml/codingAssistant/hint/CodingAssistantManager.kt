package org.jetbrains.research.ml.codingAssistant.hint

import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.hint.HintFactoryImpl
import org.jetbrains.research.ml.coding.assistant.hint.HintManager
import org.jetbrains.research.ml.coding.assistant.hint.HintManagerImpl
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceCachedRepository
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceResourcesDirectoryRepository
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.PoissonPathHintVertexCalculator
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher

object CodingAssistantManager : HintManager {
    override fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {
        return manager.getHintedFile(psiFragment, metaInfo)
    }

    private val repository = SolutionSpaceCachedRepository(
        SolutionSpaceResourcesDirectoryRepository(javaClass)
    )

    private val manager = HintManagerImpl(
        HintFactoryImpl(
            repository,
            ParallelVertexFinder(EditPartialSolutionMatcher),
            PoissonPathHintVertexCalculator
        )
    )
}
