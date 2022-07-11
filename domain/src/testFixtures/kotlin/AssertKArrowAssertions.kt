import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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

fun <A> Assert<Option<A>>.isNone(): Assert<None> = transform(appendName("none", separator = ".")) { actual ->
    when (actual) {
        is Some -> expected("to be None")
        is None -> actual
    }
}

fun <A> Assert<Option<A>>.isSome(): Assert<A> = transform(appendName("some", separator = ".")) { actual ->
    when (actual) {
        is Some -> actual.value
        is None -> expected("to be Some")
    }
}