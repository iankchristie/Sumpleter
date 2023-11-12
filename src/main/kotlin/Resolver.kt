import java.util.*

abstract class Resolver<T : Solvable, V>() {
    fun run() {
        val initialState = generateInitialState()

        val queue = LinkedList<V>()
        queue.add(initialState)

        val visited = mutableSetOf(initialState)

        while (queue.isNotEmpty()) {
            val reducedState = reduceState(queue.poll())
            println(reducedState)
            if (reducedState.isSolved()) {
                println("Complete")
                return
            }

            println("Guessing")
            val notVisitedStates = generateGuesses(reducedState).minus(visited)
            queue.addAll(notVisitedStates)
            visited.addAll(notVisitedStates)
        }
    }

    abstract fun generateInitialState(): V
    abstract fun reduceState(state: V): T
    abstract fun generateGuesses(board: T): Set<V>
}

interface Solvable {
    fun isSolved(): Boolean
}