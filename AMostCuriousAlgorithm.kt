import kotlin.math.absoluteValue

/*
    Resources used as building blocks to write program:
    https://github.com/dkandalov/kotlin-99
    https://github.com/IvanMwiruki/30-seconds-of-kotlin/blob/master/README.md
*/

fun main(args: Array<String>) {
    val results = mutableListOf<Long>()
    val current = mutableListOf(1L, 2L)
    results.add(current.max()!!)
    val resultsCount = HashMap<Long, Int>()
    repeat((0 until 7).count()) {
        val foundPrimes = computePrimes(current)
        foundPrimes.forEach { prime ->
            if (resultsCount.containsKey(prime)) {
                resultsCount[prime] = resultsCount[prime]!! + 1
            } else {
                resultsCount[prime] = 1
            }
        }
        val distinctPrimes = foundPrimes.distinct()
        println("Found primes: $distinctPrimes")
        results.addAll(distinctPrimes)
        val differenceMin = distinctPrimes.difference(current).min()!!
        current.add(differenceMin)
    }
    println("Hello primes: ${results.distinct().sorted()}")
    println("Occurence counts: ${resultsCount.toSortedMap()}")
    println("found composites: ${results.filter { !it.isPrime() }}")
}

fun Long.isPrime() = this > 1L && (2L..(this / 2)).all { this % it != 0L }

fun computePrimes(seedPrimes: List<Long>, maxExponent: Int = 4): List<Long> =
    if (seedPrimes.isEmpty()) emptyList()
    else {
        seedPrimes.binaryPartitions()
            .applyExponents(seedPrimes.size.exponentCombinations(maxExponent))
            .toSumsAndDifferences()
            .filter {
                val max = seedPrimes.max() ?: 1
                it in LongRange(max + 1, max.pow(2) - 1)
            }
            .sorted()
    }

fun <T> List<T>.difference(other: List<T>): List<T> =
    (this subtract other).toList()

fun List<List<List<Long>>>.toSumsAndDifferences(): List<Long> =
    if (isEmpty()) emptyList()
    else {
        val sumAndDifferences = mutableListOf<Long>()
        forEach {
            val firstSuccessiveProduct = it.first().reduce { acc, l -> acc * l }
            val secondSuccessiveProduct = it.last().reduce { acc, l -> acc * l }
            val sum = firstSuccessiveProduct + secondSuccessiveProduct
            sumAndDifferences.add(sum)
            val diff = (firstSuccessiveProduct - secondSuccessiveProduct).absoluteValue
            sumAndDifferences.add(diff)
        }
        sumAndDifferences
    }

fun List<List<List<Long>>>.applyExponents(exponents: List<List<Int>>): List<List<List<Long>>> =
    if (isEmpty()) emptyList()
    else {
        val primesRaisedToAPower: MutableList<List<List<Long>>> = mutableListOf()
        forEach { binaryPartitions ->
            val firstList = binaryPartitions.first()
            val secondList = binaryPartitions.last()
            exponents.forEach {
                val exponentListPair = it.split(firstList.size)
                val firstExponentList = exponentListPair.first
                val secondExponentList = exponentListPair.second
                primesRaisedToAPower.add(
                    listOf(
                        firstList.mapToPower(firstExponentList),
                        secondList.mapToPower(secondExponentList)
                    )
                )
            }
        }
        primesRaisedToAPower
    }

fun List<Long>.mapToPower(exponents: List<Int>): List<Long> =
    mapIndexed { index, item -> item.pow(exponents[index]) }

fun Long.pow(exp: Int): Long =
    if (exp <= 1) this
    else {
        var product = this
        repeat((1 until exp).count()) {
            product *= this
        }
        product
    }

fun <T> List<T>.split(n: Int): Pair<List<T>, List<T>> = Pair(take(n), drop(n))

fun Int.exponentCombinations(maxExponent: Int): List<List<Int>> =
    if (this < 1) emptyList()
    else {
        val exponents = (1..maxExponent).toList()
        val seedExponents: MutableList<List<Int>> = mutableListOf()
        repeat((1..this).count()) {
            seedExponents.add(exponents)
        }
        combinations(this, seedExponents.flatten().toList()).distinct()
    }

fun <T> List<T>.binaryPartitions(): List<List<List<T>>> =
    if (size < 2) emptyList()
    else {
        val binaryPartitions: MutableList<List<List<T>>> = mutableListOf()
        (1..this.size / 2).forEach { splitIndex ->
            binaryPartitions.addAll(group(listOf(splitIndex, this.size - splitIndex), this))
        }
        binaryPartitions
    }

fun <T> group(sizes: List<Int>, list: List<T>): List<List<List<T>>> =
    if (sizes.isEmpty()) listOf(emptyList())
    else combinations(sizes.first(), list).flatMap { combination ->
        val filteredList = list.filterNot { combination.contains(it) }
        group(sizes.tail(), filteredList).map { it + listOf(combination) }
    }

fun <T> combinations(n: Int, list: List<T>): List<List<T>> =
    if (n == 0) listOf(emptyList())
    else list.flatMapTails { subList ->
        combinations(n - 1, subList.tail()).map { (it + subList.first()) }
    }

fun <T> List<T>.flatMapTails(f: (List<T>) -> (List<List<T>>)): List<List<T>> =
    if (isEmpty()) emptyList()
    else f(this) + this.tail().flatMapTails(f)

fun <T> List<T>.tail(): List<T> = drop(1)