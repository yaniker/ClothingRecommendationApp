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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.border

import androidx.navigation.navArgument
import androidx.compose.material.icons.filled.ArrowBack

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

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

class MainActivity : ComponentActivity() {
    private lateinit var outfitModel: OutfitModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outfitModel = OutfitModel(this)

        setContent {
            WorkCombinationRecommendTheme {
                val navController = rememberNavController()
                var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

                NavHost(navController = navController, startDestination = "recommendation") {
                    composable("recommendation") {
                        RecommendationScreen(
                            model = outfitModel,
                            navController = navController,
                            selectedId = selectedId,
                            onClearSelection = { selectedId = null } // ✅ This is now the mutation source
                        )
                    }
                    composable("wardrobe") {
                        WardrobeScreen(navController, onConfirm = { id ->
                            selectedId = id
                            navController.popBackStack() // 💥 pop instead of navigate
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    model: OutfitModel,
    navController: NavController,
    selectedId: String?,
    onClearSelection: () -> Unit
){
    val context = LocalContext.current
    val items = remember { loadClothingData(context) }

    var topItem by remember { mutableStateOf<ClothingItem?>(null) }
    var bottomItem by remember { mutableStateOf<ClothingItem?>(null) }
    var message by remember { mutableStateOf("Tap to get recommendation") }
    var userPrompt by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outfit Recommender") },
                actions = {
                    IconButton(onClick = { navController.navigate("wardrobe") }) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = "Wardrobe"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 👕 Show selected item (if any) above the prompt
            selectedId?.let { id ->
                val selectedItem = items.find { it.id == id }
                selectedItem?.let { item ->
                    val context = LocalContext.current
                    val assetManager = context.assets
                    val bitmap = remember(item.id) {
                        val input = assetManager.open("${item.type}/${item.id}.jpeg")
                        val original = BitmapFactory.decodeStream(input)
                        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                        Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected item",
                            modifier = Modifier.size(100.dp)
                        )

                        IconButton(onClick = { onClearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    }
                }
            }

            // 🆕 User prompt input (moved above)
            Text("Optional: type a style preference below:")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                label = { Text("Enter your preference") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🧠 Unified recommendation button
            Button(onClick = {
                val pairs = if (selectedId != null) {
                    getShuffledPairs(items).filter {
                        it.first.id == selectedId || it.second.id == selectedId
                    }
                } else {
                    getShuffledPairs(items)
                }

                var filteredPairs = pairs

                if (userPrompt.isNotBlank()) {
                    // TODO: Add AWS LLM API call here and update filteredPairs
                    message = "Sending prompt to LLM backend: \"$userPrompt\""
                    // You'll replace this with real filtered results once backend is connected
                }

                var found = false

                for ((top, bottom) in filteredPairs) {
                    val input = floatArrayOf(
                        colorMap[top.color1]!!.toFloat(), patternMap[top.pattern]!!.toFloat(),
                        materialMap[top.material]!!.toFloat(), fitMap[top.fit]!!.toFloat(),
                        colorMap[bottom.color1]!!.toFloat(), patternMap[bottom.pattern]!!.toFloat(),
                        materialMap[bottom.material]!!.toFloat(), fitMap[bottom.fit]!!.toFloat()
                    )
                    val result = model.predict(input)
                    if (result >= 0.5f) {
                        topItem = top
                        bottomItem = bottom
                        message = "Showing recommended outfit"
                        found = true
                        break
                    }
                }

                if (!found) {
                    message = "No recommended combinations found"
                    topItem = null
                    bottomItem = null
                }
            }) {
                Text("Get Recommendation")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(message)

            topItem?.let {
                Spacer(modifier = Modifier.height(10.dp))
                ImageFromAssets(fileName = "${it.type}/${it.id}.jpeg")
            }

            bottomItem?.let {
                Spacer(modifier = Modifier.height(10.dp))
                ImageFromAssets(fileName = "${it.type}/${it.id}.jpeg")
            }
        }
    }
}
