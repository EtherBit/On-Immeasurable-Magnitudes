/* prime_number_algorithm_2.kt */

import kotlin.math.absoluteValue
import kotlin.math.pow

val MAX_ITERATIONS = 8
val MAX_EXPONENT = 5 // This maximum exponent is applied only to one subset.
val MAX_BOUND = 100000
var currentIterationIndex = 0
var currentBatchOfImmeasurables = mutableListOf<Long>() // The list of immeasurables, which gets collected and cleared after every iteration.
var allImmeasurables = mutableListOf<Long>() // All immeasurables found.
var currentLadderStep = 0L // The biggest element used to calculate  C (the upper bound below which calculated immeasurables are prime).
val countMap = HashMap<Long, Int>() // A map used to count the number of times an immeasurable is found.
var exponentsMap = HashMap<Int, MutableList<MutableList<Int>>>() // A map that stores previously computed exponent variations.

fun main(args: Array<String>) {
    val seedImmeasurables = mutableListOf(1L, 2L)
    val seedImmeasurableString = getSetString(seedImmeasurables)
    println("Given the seed set $seedImmeasurableString")
    allImmeasurables.addAll(seedImmeasurables)
    currentLadderStep = seedImmeasurables.max()!!
    yieldImmeasurables(seedImmeasurables = seedImmeasurables, splitSwapIndex = 0)
}

fun getSetString(list: List<Any>) = list.toString().replace("[", "{").replace("]", "}")

// A function that generates exponents in an incremental manner for a specified size of magnitudes that need to be raised to a power.
fun generateExponents(numberOfImmeasurableToBeRaised: Int): MutableList<MutableList<Int>> {
    val variations = mutableListOf<MutableList<Int>>()
    val currentVariation = MutableList(numberOfImmeasurableToBeRaised) { 1 }
    val lastVariationAccumulatedProduct = List(numberOfImmeasurableToBeRaised) { MAX_EXPONENT }.reduce { accumulatedProduct, i -> accumulatedProduct * i }
    var index = 0
    variations.add(currentVariation.toMutableList())
    while (currentVariation.reduce { accumulatedProduct, i -> accumulatedProduct * i } < lastVariationAccumulatedProduct) {
        while (currentVariation[numberOfImmeasurableToBeRaised - 1 - index] + 1 > MAX_EXPONENT) {
            if (numberOfImmeasurableToBeRaised - 1 - index > 0) {
                currentVariation[numberOfImmeasurableToBeRaised - 1 - index] = 1
            }
            ++index
        }
        if (index > numberOfImmeasurableToBeRaised - 1) {
            index = 0
        }
        currentVariation[numberOfImmeasurableToBeRaised - 1 - index] += 1
        index = 0
        variations.add(currentVariation.toMutableList())
    }
    return variations
}

fun isWithinBound(found: Long, upperBound: Long) = found < upperBound

fun addFoundImmeasurableToMap(found: Long) {
    if (countMap.containsKey(found)) {
        countMap[found] = countMap[found]!! + 1
    } else {
        countMap[found] = 1
    }
}

// The main function which recursively yields immeasurables by using a split index as a swap pivot to generate the two sets or lists of immeasurables.
fun yieldImmeasurables(seedImmeasurables: MutableList<Long>, splitSwapIndex: Int, isLeaf: Boolean = false) {
    val sizeOfCurrentSeedImmeasurables = seedImmeasurables.size
    if (sizeOfCurrentSeedImmeasurables < 2 || currentIterationIndex >= MAX_ITERATIONS) {
        return
    }
    var swapCursor = splitSwapIndex + 1
    while (swapCursor < sizeOfCurrentSeedImmeasurables + 1) {
        val leftCollection = seedImmeasurables.subList(0, splitSwapIndex + 1)
        val rightCollection = seedImmeasurables.subList(splitSwapIndex + 1, sizeOfCurrentSeedImmeasurables)
        // primeFulcrum is the successive product of the set that is constrained to never be raised above the power of 1.
        val primeFulcrum = rightCollection.reduce { acc, i -> acc * i }
        // This generates exponents if not previously stored.
        val listOfExponents = if (exponentsMap.containsKey(leftCollection.size)) {
            exponentsMap[leftCollection.size]
        } else {
            exponentsMap[leftCollection.size] = generateExponents(numberOfImmeasurableToBeRaised = leftCollection.size)
            exponentsMap[leftCollection.size]
        }
        listOfExponents!!.forEach { exponentsList ->
            val raisedLeftCollection = leftCollection.mapIndexed { index, prime -> (prime.toDouble().pow(exponentsList[index].toDouble())).toInt() }.toMutableList()
            val leftRepeatedMultiplications = raisedLeftCollection.reduce { acc, i -> acc * i }
            if (leftRepeatedMultiplications < 0 || primeFulcrum < 0) {
                return@forEach
            }
            val firstPrime = leftRepeatedMultiplications + primeFulcrum
            val secondPrime = (leftRepeatedMultiplications - primeFulcrum).absoluteValue
            val currentUpperBound = currentLadderStep * currentLadderStep
            if (isWithinBound(firstPrime, currentUpperBound)) {
                currentBatchOfImmeasurables.add(firstPrime)
                addFoundImmeasurableToMap(firstPrime)
            }
            if (isWithinBound(secondPrime, currentUpperBound)) {
                currentBatchOfImmeasurables.add(secondPrime)
                addFoundImmeasurableToMap(secondPrime)
            }
        }
        val tempImmeasurables = mutableListOf<Long>()
        tempImmeasurables.addAll(seedImmeasurables)
        if (splitSwapIndex + 1 < sizeOfCurrentSeedImmeasurables / 2) {
            yieldImmeasurables(tempImmeasurables, splitSwapIndex + 1)
        }
        if (swapCursor < sizeOfCurrentSeedImmeasurables) {
            seedImmeasurables[splitSwapIndex] = seedImmeasurables[splitSwapIndex] xor seedImmeasurables[swapCursor]
            seedImmeasurables[swapCursor] = seedImmeasurables[splitSwapIndex] xor seedImmeasurables[swapCursor]
            seedImmeasurables[splitSwapIndex] = seedImmeasurables[splitSwapIndex] xor seedImmeasurables[swapCursor]
        } else if (swapCursor == sizeOfCurrentSeedImmeasurables) {
            if (splitSwapIndex == 0 && currentLadderStep < MAX_BOUND && !isLeaf) {
                currentIterationIndex++
                val foundImmeasurables = currentBatchOfImmeasurables.distinct().filter { it !in allImmeasurables }
                allImmeasurables.addAll(foundImmeasurables)
                currentLadderStep = allImmeasurables.filter { it > currentLadderStep }.min()!!
                currentBatchOfImmeasurables.clear()
                foundImmeasurables.filter { it != currentLadderStep }.forEach {
                    val leafSeedImmeasurables = seedImmeasurables.distinct().toMutableList()
                    leafSeedImmeasurables.add(it)
                    yieldImmeasurables(leafSeedImmeasurables, 0, true)
                }
                val newSeedImmeasurables = seedImmeasurables.toMutableList()
                newSeedImmeasurables.add(currentLadderStep)
                println("At iteration $currentIterationIndex this program found the following immeasurables: ${getSetString(allImmeasurables.sorted())},")
                var primeCountString = allImmeasurables.sorted().filter { it != 2L }.map { prime ->
                    "$prime was found ${countMap[prime]} times"
                }.toString().replace("[", "").replace("]", "")
                val lastIndexOfComma = primeCountString.lastIndexOf(",")
                primeCountString = primeCountString.replaceRange(lastIndexOfComma + 1, lastIndexOfComma + 2, " and ")
                println("where $primeCountString.")
                yieldImmeasurables(newSeedImmeasurables, 0)
            }
        }
        swapCursor++
    }
}
