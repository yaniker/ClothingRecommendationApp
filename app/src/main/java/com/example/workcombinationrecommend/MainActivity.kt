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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity

val ClothingItemSaver: Saver<ClothingItem?, Map<String, Any>> = Saver(
    save = { item ->
        item?.let {
            mapOf(
                "id" to it.id,
                "type" to it.type,
                "color1" to it.color1,
                "color2" to it.color2,
                "pattern" to it.pattern,
                "dress_code" to it.dress_code,
                "material" to it.material,
                "seasonality" to it.seasonality,
                "fit" to it.fit
            )
        }
    },
    restore = { map ->
        map?.let {
            ClothingItem(
                id = it["id"] as String,
                type = it["type"] as String,
                color1 = it["color1"] as String,
                color2 = it["color2"] as String,
                pattern = it["pattern"] as String,
                dress_code = it["dress_code"] as String,
                material = it["material"] as String,
                seasonality = it["seasonality"] as String,
                fit = it["fit"] as String
            )
        }
    }
)

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
                val selectedItemState = rememberSaveable(stateSaver = ClothingItemSaver) {
                    mutableStateOf<ClothingItem?>(null)
                }

                NavHost(navController = navController, startDestination = "recommendation") {
                    composable("recommendation") {
                        RecommendationScreen(
                            model = outfitModel,
                            navController = navController,
                            selectedItem = selectedItemState.value,
                            onClearSelection = { selectedItemState.value = null }
                        )
                    }
                    composable("wardrobe") {
                        WardrobeScreen(navController, onConfirm = { item ->
                            selectedItemState.value = item
                            navController.popBackStack()
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
    selectedItem: ClothingItem?,
    onClearSelection: () -> Unit
)
{
    val context = LocalContext.current
    val items = remember { loadClothingData(context) }
    val imageSize = remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val boxModifier = if (imageSize.value > 0.dp) {
        Modifier
            .width(imageSize.value)
            .aspectRatio(1f)
    } else {
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                with(density) {
                    val measured = coords.size.width.toDp()
                    if (measured > imageSize.value) {
                        imageSize.value = measured
                    }
                }
            }
    }

    var topItem by remember { mutableStateOf<ClothingItem?>(null) }
    var bottomItem by remember { mutableStateOf<ClothingItem?>(null) }
    var message by remember { mutableStateOf("") }
    var userPrompt by remember { mutableStateOf("") }

    LaunchedEffect(selectedItem) {
        if (selectedItem != null) {
            if (selectedItem.type == "top") {
                topItem = selectedItem
                bottomItem = null
            } else {
                bottomItem = selectedItem
                topItem = null
            }
        } else {
            topItem = null
            bottomItem = null
        }
    }

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

            // 🆕 User prompt input (moved above)
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
                val prompt = userPrompt.trim().lowercase()
                val topPrefix = "top:"
                val bottomPrefix = "bottom:"

                val promptTopAttrs: List<String>
                val promptBottomAttrs: List<String>

                val hasTopPrompt = prompt.startsWith(topPrefix)
                val hasBottomPrompt = prompt.startsWith(bottomPrefix)

                promptTopAttrs = if (hasTopPrompt) {
                    prompt.removePrefix(topPrefix).split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else emptyList()

                promptBottomAttrs = if (hasBottomPrompt) {
                    prompt.removePrefix(bottomPrefix).split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else emptyList()

                val tops = when {
                    selectedItem?.type == "top" -> listOf(selectedItem)
                    promptTopAttrs.isNotEmpty() -> items.filter { it.type == "top" && itemMatchesPrompt(it, promptTopAttrs) }
                    else -> items.filter { it.type == "top" }
                }

                val bottoms = when {
                    selectedItem?.type == "bottom" -> listOf(selectedItem)
                    promptBottomAttrs.isNotEmpty() -> items.filter { it.type == "bottom" && itemMatchesPrompt(it, promptBottomAttrs) }
                    else -> items.filter { it.type == "bottom" }
                }

                val candidates = tops.flatMap { top ->
                    bottoms.map { bottom -> Pair(top, bottom) }
                }.shuffled()

                var found = false

                for ((top, bottom) in candidates) {
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
                        message = "Recommended combination"
                        found = true
                        break
                    }
                }

                if (!found) {
                    message = "No suitable pair found"
                    if (selectedItem?.type == "top") bottomItem = null
                    else if (selectedItem?.type == "bottom") topItem = null
                    else {
                        topItem = null
                        bottomItem = null
                    }
                }
            }) {
                Text("Get Recommendation")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(message)

            topItem?.let { item ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = boxModifier) {
                    ImageFromAssets(fileName = "${item.type}/${item.id}.jpeg")
                    if (item.id == selectedItem?.id) {
                        IconButton(
                            onClick = { onClearSelection() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear selection",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            bottomItem?.let { item ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = boxModifier) {
                    ImageFromAssets(fileName = "${item.type}/${item.id}.jpeg")
                    if (item.id == selectedItem?.id) {
                        IconButton(
                            onClick = { onClearSelection() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear selection",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

fun itemMatchesPrompt(item: ClothingItem, attributes: List<String>): Boolean {
    val attrs = listOf(
        item.color1,
        item.color2,
        item.pattern,
        item.material,
        item.seasonality,
        item.dress_code,
        item.fit
    ).map { it.lowercase().trim() }

    return attributes.all { token -> token in attrs }
}


