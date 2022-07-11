import arrow.core.Either
import assertk.Assert
import assertk.assertions.support.appendName
import assertk.assertions.support.expected

fun <A,B> Assert<Either<A, B>>.isRight(): Assert<B> = transform(appendName("right", separator = ".")) { actual ->
    when (actual) {
        is Either.Left -> expected("to be right")
        is Either.Right -> actual.value
    }
}

fun <A,B> Assert<Either<A, B>>.isLeft(): Assert<A> = transform(appendName("left", separator = ".")) { actual ->
    when (actual) {
        is Either.Left -> actual.value
        is Either.Right -> expected("to be left")
    }
}