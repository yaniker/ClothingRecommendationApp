package com.example.workcombinationrecommend

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import java.io.IOException
import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn

fun loadImageFromAssets(assetManager: android.content.res.AssetManager, path: String): Bitmap? {
    return try {
        val input = assetManager.open(path)
        val original = BitmapFactory.decodeStream(input)
        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
        Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    } catch (e: IOException) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(navController: NavController) {
    val context = LocalContext.current
    val assetManager = context.assets
    val clothingItems = remember { loadClothingData(context) }

    var selectedId by remember { mutableStateOf<String?>(null) }

    val tops = clothingItems.filter { it.type == "top" }
    val bottoms = clothingItems.filter { it.type == "bottom" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wardrobe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .padding(16.dp)
        ) {
            // SCROLLABLE AREA
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Tops", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                    ) {
                        items(tops) { item ->
                            val bitmap = loadImageFromAssets(assetManager, "top/${item.id}.jpeg")
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(
                                            width = 3.dp,
                                            color = if (selectedId == item.id) Color.Red else Color.Transparent
                                        )
                                        .clickable { selectedId = item.id }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bottoms", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                    ) {
                        items(bottoms) { item ->
                            val bitmap = loadImageFromAssets(assetManager, "bottom/${item.id}.jpeg")
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(
                                            width = 3.dp,
                                            color = if (selectedId == item.id) Color.Red else Color.Transparent
                                        )
                                        .clickable { selectedId = item.id }
                                )
                            }
                        }
                    }
                }
            }

            // FIXED BUTTON AT BOTTOM
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    selectedId?.let {
                        navController.navigate("recommendation?selected=$it")
                    }
                },
                enabled = selectedId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm")
            }
        }
    }
}