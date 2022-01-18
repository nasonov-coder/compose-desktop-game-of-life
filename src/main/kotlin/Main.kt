// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
//import androidx.compose.ui.semantics.Role.Companion.Image
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.properties.Delegates

//import org.jetbrains.skia.Image

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, Worldsds!") }

    MaterialTheme {
        Column {
//            Button(onClick = {
//                text = "Hello, Desktopasd!"
//            }) {
//                Text(text)
//            }
            MessageCard(Message(text, text))
        }

    }
}

fun main() = application {

    Window(onCloseRequest = ::exitApplication) {
//        MenuBar {
//            this.Menu("") {
//                this.
//            }
//        }
        App()

    }
}


data class Message(val author: String, val body: String)
@Preview
@Composable
fun Menu() {

    var gliderToggle by remember { mutableStateOf(TableCreator.gliderToggle) }
    var popup by remember { mutableStateOf(false) }
    Row {

        Button(onClick = {
            TableCreator.recreate()
        }) {
            Text("Restart")
        }
        Checkbox(
            checked = gliderToggle, onCheckedChange = {
                TableCreator.gliderToggle = it
                gliderToggle = it
            }
        )
        Button(onClick = {
            TableCreator.addGlider()
        }) {
            Text("add glider")
        }

    }
}
@Composable
fun MessageCard(msg: Message) {

    Column {
        var canvasWidth = 0f
        var canvasHeight = 0f
//        Image(
//            painter = painterResource(R.drawable.profile_picture),
//            contentDescription = "Contact profile picture",
//        )
        var recSize = 10f

        Menu()


//        Column {
//            Text(text = msg.author)
//            Text(text = msg.body)
//        }

        EachFrameUpdatingCanvas(Modifier.fillMaxSize()) { frameTime, table ->
            canvasWidth = this.size.width
            canvasHeight = this.size.height
//            drawLine(
//                start = Offset(x = canvasWidth, y = 0f),
//                end = Offset(x = 0f, y = canvasHeight),
//                color = Color.Blue
//            )
            table.forEachIndexed { i, row ->
                row.forEachIndexed { j, cell ->
                    if( cell )
                        drawRect(
                            Color.Black,
                            topLeft = Offset(i * recSize, j * recSize),
                            size = Size(recSize, recSize)
                        )
                }
            }
            TableCreator.start((canvasHeight/recSize).toInt(),  (canvasWidth/recSize).toInt())

//            val height = (this.size.height / recSize).toInt()
//            if(height < table.size)
//                table = table.dropLast(height - table.size)
//            else if( height > table.size ) {
//                table = table +
//            }
        }
    }
//    Canvas(modifier = Modifier.fillMaxSize()) {
//
//        val canvasWidth = size.width
//        val canvasHeight = size.height
//
//        drawLine(
//            start = Offset(x = canvasWidth, y = 0f),
//            end = Offset(x = 0f, y = canvasHeight),
//            color = Color.Blue
//        )
//    }
}

@Preview
@Composable
fun PreviewMessageCard() {
    MessageCard(
        msg = Message("Colleague", "Hey, take a look at Jetpack Compose, it's great!")
    )
}
typealias Table = List<List<Boolean>>

object TableCreator {
    var gliderCreationSpeed = 7
    private val tableContext = newSingleThreadContext("tableContext")
    val tableChannel = Channel<Table>(Channel.RENDEZVOUS)
//    val tableReady = Channel<Unit>(Channel.RENDEZVOUS)
    private val scope = CoroutineScope(tableContext)
    private var job: Job? = null
    var h by Delegates.notNull<Int>()
    var w by Delegates.notNull<Int>()
    private lateinit var table: Table
    var gliderToggle = false
    private var gliderToggleCount = gliderCreationSpeed
    fun addGlider() {
        (table as MutableList<MutableList<Boolean>>).also { table ->
            table[0][0] = false
            table[0][1] = false
            table[0][2] = true
            table[1][0] = true
            table[1][1] = false
            table[1][2] = true
            table[2][0] = true
            table[2][0] = false
            table[2][1] = true
            table[2][2] = true
        }
    }
    fun start(h: Int, w: Int) {
        this.h = h
        this.w = w
        if(job!= null)
            return
        job = scope.launch {
            recreate()
            while(true) {
                table = table.mapIndexed { i, row ->
                    row.mapIndexed { j, cell ->
                        var count = 0
                        (i-1..i+1).forEach { x ->
                            (j-1..j+1).forEach kek@{ y ->
                                if(x == i && y == j)
                                    return@kek
                                val neighbour = table.getOrNull(x)?.getOrNull(y)
                                neighbour?.let {
                                    if(it)
                                        count++
                                }
                            }
                        }

                        if(cell) {
                            count in 2..3
                        } else {
                            count == 3
                        }
                    }
                }
                tableChannel.send(table)
                if(gliderToggle) {
                    gliderToggleCount--
                    if(gliderToggleCount <= 0) {
                        gliderToggleCount = gliderCreationSpeed
                        addGlider()
                    }
                }
            }
        }
    }
    fun recreate() {
        table = List(w.toInt()) {
            List(h.toInt()) { listOf(false, false, false,false, false, false, true, true,  true).random() }
        }
    }


}
@Composable
fun EachFrameUpdatingCanvas(modifier: Modifier, onDraw: DrawScope.(Long, Table) -> Unit) {
    var frameTime by remember { mutableStateOf(0L) }

    var table: Table = listOf(listOf())
    LaunchedEffect(Unit) {

            while (true) {
                // this will be called for each frame
                // by updating `remember` value we initiating EachFrameUpdatingCanvas redraw
                delay(100)
                table = TableCreator.tableChannel.receive()
                frameTime++
//                frameTime = withFrameMillis { it } // on eachFrame
            }

    }
    Canvas(modifier = modifier) {
        // you had to use frameTime somewhere in EachFrameUpdatingCanvas
        // otherwise it won't be redrawn. But you don't have to pass it to `onDraw` if you don't want
        onDraw(frameTime, table)
    }
}