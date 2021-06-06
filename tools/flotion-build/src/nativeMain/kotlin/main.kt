import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int

class Hello : CliktCommand() {
	val count: Int by option(help="Number of greetings").int().default(1)

	override fun run() {
		repeat(count) {
			echo("Hello!")
		}
	}
}

fun main(args: Array<String>) = Hello().main(args)
