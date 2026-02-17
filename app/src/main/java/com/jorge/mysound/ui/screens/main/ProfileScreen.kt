package com.jorge.mysound.ui.screens.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.ui.AppViewModelFactory
import com.jorge.mysound.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    userId: Long,
    factory: AppViewModelFactory,
    viewModel: ProfileViewModel = viewModel(factory = factory),
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val user by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Carga de datos inicial
    LaunchedEffect(userId) {
        viewModel.fetchProfile(userId)
    }

    // Selector de imágenes de la galería
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.updateUserAvatar(context, it, userId) }
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading && user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp), // Espacio para el MiniPlayer
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HEADER INTERNACIONALIZADO
                ProfileHeader(
                    avatarUrl = user?.avatarUrl,
                    username = user?.username ?: stringResource(R.string.profile_default_user),
                    email = user?.email ?: stringResource(R.string.loading),
                    onAvatarClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // STATS GRID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(
                        label = stringResource(R.string.profile_top_genre),
                        value = user?.favoriteGenre ?: "—"
                    )
                    ProfileStatItem(
                        label = stringResource(R.string.profile_total_songs),
                        value = user?.totalSongsInPlaylists?.toString() ?: "0"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // MENÚ DE OPCIONES
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

                    Text(
                        text = stringResource(R.string.profile_section_account),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Reutilizamos el string de Settings que creamos antes
                    ProfileOptionItem(
                        icon = Icons.Default.Settings,
                        title = stringResource(R.string.settings_title),
                        onClick = onSettingsClick
                    )

                    ProfileOptionItem(
                        icon = Icons.Default.ExitToApp,
                        title = stringResource(R.string.profile_logout),
                        isDanger = true,
                        onClick = onLogoutClick
                    )
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
private fun ProfileHeader(
    avatarUrl: String?,
    username: String,
    email: String,
    onAvatarClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 32.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {

            // 🔥 LÓGICA DE URL ROBUSTA (Igual que en Home)
            val imageUrl = remember(avatarUrl) {
                if (avatarUrl.isNullOrEmpty()) {
                    "https://api.dicebear.com/7.x/avataaars/svg?seed=$username&backgroundColor=b6e3f4"
                } else if (avatarUrl.startsWith("http")) {
                    avatarUrl
                } else {
                    val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
                    val path = avatarUrl.removePrefix("/")
                    "$baseUrl/$path"
                }
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    // Truco del almendruco para invalidar caché al subir foto nueva
                    .setParameter("timestamp", System.currentTimeMillis())
                    .build(),
                contentDescription = stringResource(R.string.profile_edit_avatar),
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onAvatarClick() },
                contentScale = ContentScale.Crop,
                // Asegúrate de tener un drawable placeholder o usa uno del sistema
                error = painterResource(R.drawable.ic_music_note),
                placeholder = painterResource(R.drawable.ic_music_note)
            )

            // Botón de editar (Lápiz)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_edit_avatar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "@$username",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileStatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}