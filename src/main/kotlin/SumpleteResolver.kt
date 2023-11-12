import SumpleteBoard.BoardEntry
import SumpleteBoard.State
import SumpleteBoard.State.*
import SumpleteResolver.Check
import SumpleteResolver.Direction.COLUMN
import SumpleteResolver.Direction.ROW
import SumpleteResolver.ReduceResult.Anomaly
import SumpleteResolver.ReduceResult.ChangeList

data class SearchState(val board: SumpleteBoard, val checklist: MutableSet<Check>)

class SumpleteResolver(private val board: SumpleteBoard) : Resolver<SumpleteBoard, SearchState>() {
    override fun generateInitialState(): SearchState {
        val checkNext = mutableSetOf<Check>()
        for (i in (0 until board.size())) {
            checkNext.add(Check(i, ROW))
            checkNext.add(Check(i, COLUMN))
        }

        return SearchState(board, checkNext)
    }

    override fun reduceState(state: SearchState): SumpleteBoard {
        val board = state.board
        val checklist = state.checklist

        val boardBuffer = board.entries.map { it.toTypedArray() }.toTypedArray()
        while (checklist.isNotEmpty()) {
            val check = checklist.first()
            checklist.remove(check)
            when (check.direction) {
                ROW -> {
                    when (val reduceResult =
                        reduceVector(boardBuffer.row(check.index).asList(), board.rowSums[check.index])) {
                        Anomaly -> {} // do nothing
                        is ChangeList -> {
                            reduceResult.changes.forEach {
                                boardBuffer[check.index][it.index] =
                                    boardBuffer[check.index][it.index].copy(state = it.state)
                                checklist.add(Check(it.index, COLUMN))
                            }
                        }
                    }
                }
                COLUMN -> {
                    when (val reduceResult =
                        reduceVector(boardBuffer.column(check.index).asList(), board.colSums[check.index])) {
                        Anomaly -> {} // do nothing
                        is ChangeList -> {
                            reduceResult.changes.forEach {
                                boardBuffer[it.index][check.index] =
                                    boardBuffer[it.index][check.index].copy(state = it.state)
                                checklist.add(Check(it.index, ROW))
                            }
                        }
                    }
                }
            }
        }

        return board.copy(entries = boardBuffer.map { it.asList() })
    }

    override fun generateGuesses(board: SumpleteBoard): Set<SearchState> {
        return board.entries.matrixFoldIndexed(mutableSetOf()) { row, col, acc, entry ->
            if (entry.state == UNKNOWN) {
                val checkNext = mutableSetOf<Check>()
                checkNext.add(Check(row, ROW))
                checkNext.add(Check(col, COLUMN))

                // Include
                val includeEntry = entry.copy(state = INCLUDED)
                val includeCopy = board.entries.toMutableCopy()
                includeCopy[row][col] = includeEntry
                val includeState = SearchState(
                    board.copy(entries = includeCopy),
                    checkNext
                )
                acc.add(includeState)

                // Exclude
                val excludeEntry = entry.copy(state = EXCLUDED)
                val excludeCopy = board.entries.toMutableCopy()
                excludeCopy[row][col] = excludeEntry
                val excludeState = SearchState(
                    board.copy(entries = excludeCopy),
                    checkNext
                )
                acc.add(excludeState)
            }
            return@matrixFoldIndexed acc
        }
    }

    private fun reduceVector(vector: List<BoardEntry>, targetSum: Int): ReduceResult {
        val knownSum = vector.sumOf { if (it.state == INCLUDED) it.value else 0 }
        val indexAndValues = vector.mapIndexedNotNull { index, entry ->
            return@mapIndexedNotNull if (entry.state == UNKNOWN) {
                IndexAndValues(index = index, value = entry.value)
            } else {
                null
            }
        }
        return changeList(indexAndValues, targetSum - knownSum)
    }

    private fun changeList(entries: List<IndexAndValues>, target: Int): ReduceResult {
        val numCombinations = (1 shl entries.size)
        val validCombinations = (0 until numCombinations).filter { combination ->
            val sumCombination = entries.foldIndexed(0) { index, acc, (_, num) ->
                return@foldIndexed acc + (num * ((combination shr index) and 1))
            }
            sumCombination == target
        }

        if (target != 0 && validCombinations.isEmpty()) {
            return Anomaly
        }

        val mustInclude = validCombinations.reduce { acc, i -> return@reduce acc and i }
        val mustExclude = validCombinations.map { it.inv() }.reduce { acc, i -> return@reduce acc and i }

        val includeChanges = entries.filterIndexed { index, _ ->
            return@filterIndexed ((mustInclude shr index) and 1) == 1
        }.map { Change(it.index, INCLUDED) }

        val excludeChanges = entries.filterIndexed { index, _ ->
            return@filterIndexed ((mustExclude shr index) and 1) == 1
        }.map { Change(it.index, EXCLUDED) }

        return ChangeList(listOf(includeChanges, excludeChanges).flatten())
    }

    sealed class ReduceResult {
        data class ChangeList(val changes: List<Change>) : ReduceResult()
        object Anomaly : ReduceResult()
    }

    data class Change(val index: Int, val state: State)

    data class Check(val index: Int, val direction: Direction)

    enum class Direction {
        ROW, COLUMN
    }

    data class IndexAndValues(val index: Int, val value: Int)
}
