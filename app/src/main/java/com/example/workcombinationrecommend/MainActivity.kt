package com.example.workcombinationrecommend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workcombinationrecommend.ui.theme.WorkCombinationRecommendTheme
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    private lateinit var outfitModel: OutfitModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outfitModel = OutfitModel(this)

        setContent {
            WorkCombinationRecommendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecommendationScreen(outfitModel)
                }
            }
        }
    }
}

@Composable
fun RecommendationScreen(model: OutfitModel) {
    val context = LocalContext.current
    val items = remember { loadClothingData(context) }

    var topId by remember { mutableStateOf<String?>(null) }
    var bottomId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("Tap to get recommendation") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            val pairs = getShuffledPairs(items)
            var found = false

            for ((top, bottom) in pairs) {
                val input = floatArrayOf(
                    colorMap[top.color1]!!.toFloat(), patternMap[top.pattern]!!.toFloat(),
                    materialMap[top.material]!!.toFloat(), fitMap[top.fit]!!.toFloat(),
                    colorMap[bottom.color1]!!.toFloat(), patternMap[bottom.pattern]!!.toFloat(),
                    materialMap[bottom.material]!!.toFloat(), fitMap[bottom.fit]!!.toFloat()
                )
                val result = model.predict(input)
                if (result >= 0.5f) {
                    topId = top.id
                    bottomId = bottom.id
                    message = "Showing recommended outfit"
                    found = true
                    break
                }
            }

            if (!found) {
                message = "No recommended combinations found"
                topId = null
                bottomId = null
            }
        }) {
            Text("Get Recommendation")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(message)

        topId?.let {
            ImageFromAssets(fileName = "$it.jpeg")
        }

        bottomId?.let {
            Spacer(modifier = Modifier.height(10.dp))
            ImageFromAssets(fileName = "$it.jpeg")
        }
    }
}

fun loadClothingData(context: Context): List<ClothingItem> {
    val json = context.assets.open("attributes_new.json").bufferedReader().use { it.readText() }
    val gson = com.google.gson.Gson()
    val items = gson.fromJson(json, Array<ClothingItem>::class.java)
    return items.toList()
}

val colorMap = mapOf(
    "red" to 0, "blue" to 1, "white" to 2, "black" to 3,
    "brown" to 4, "green" to 5, "yellow" to 6, "gray" to 7,
    "navy" to 8, "pink" to 9, "none" to 10
)

val patternMap = mapOf("solid" to 0, "striped" to 1, "floral" to 2, "plaid" to 3, "polka dot" to 4)
val materialMap = mapOf("cotton" to 0, "denim" to 1, "silk" to 2, "wool" to 3, "linen" to 4, "polyester" to 5, "unknown" to 6)
val fitMap = mapOf("loose" to 0, "relaxed" to 1, "fitted" to 2, "tailored" to 3, "slim" to 4)

fun getShuffledPairs(items: List<ClothingItem>): List<Pair<ClothingItem, ClothingItem>> {
    val tops = items.filter { it.type == "top" }
    val bottoms = items.filter { it.type == "bottom" }

    return tops.flatMap { top ->
        bottoms.map { bottom -> Pair(top, bottom) }
    }.shuffled()
}

@Composable
fun ImageFromAssets(fileName: String) {
    val context = LocalContext.current
    val assetManager = context.assets

    val bitmap = remember(fileName) {
        val input = assetManager.open(fileName)
        val original = BitmapFactory.decodeStream(input)

        val matrix = android.graphics.Matrix().apply {
            postRotate(90f)
        }

        Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}
