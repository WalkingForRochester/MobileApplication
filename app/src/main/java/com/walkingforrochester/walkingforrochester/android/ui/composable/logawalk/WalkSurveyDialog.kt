package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.CameraCapture
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.FullScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.HorizontalNumberPicker
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.RequestPermissions
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRDialog
import com.walkingforrochester.walkingforrochester.android.ui.state.SurveyDialogState
import com.walkingforrochester.walkingforrochester.android.ui.theme.MaterialRed
import com.walkingforrochester.walkingforrochester.android.viewmodel.LogAWalkViewModel

@Composable
fun WalkSurveyDialog(
    modifier: Modifier = Modifier,
    logAWalkViewModel: LogAWalkViewModel,
    surveyDialogState: SurveyDialogState
) {
    WFRDialog(
        modifier = modifier.padding(horizontal = 8.dp),
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        contentTextStyle = MaterialTheme.typography.bodyLarge,
        buttonsArrangement = Arrangement.SpaceAround,
        onDismissRequest = logAWalkViewModel::onDismissSurveyDialog,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_finish),
                contentDescription = null
            )
        },
        title = {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.are_you_finished),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        buttons = {
            SurveyButtons(
                logAWalkViewModel = logAWalkViewModel,
                surveyDialogState = surveyDialogState
            )
        }
    ) {
        SurveyContent(logAWalkViewModel = logAWalkViewModel, surveyDialogState = surveyDialogState)
    }
}

@Composable
private fun SurveyButtons(
    logAWalkViewModel: LogAWalkViewModel,
    surveyDialogState: SurveyDialogState
) {
    val contentPadding = PaddingValues(
        top = ButtonDefaults.ContentPadding.calculateTopPadding(),
        start = 16.dp,
        bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
        end = 16.dp
    )
    OutlinedButton(
        onClick = logAWalkViewModel::onDismissSurveyDialog,
        modifier = Modifier.height(56.dp),
        contentPadding = contentPadding
    ) {
        Text(
            text = stringResource(id = R.string.continue_walking),
            style = LocalTextStyle.current.copy(lineBreak = LineBreak.Simple),
            textAlign = TextAlign.Center
        )
    }
    Button(
        onClick = logAWalkViewModel::onDiscardWalking,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialRed,
            contentColor = Color.White
        ),
        modifier = Modifier.height(56.dp),
        contentPadding = contentPadding
    ) {
        Text(stringResource(id = R.string.discard))
    }
    Button(
        onClick = logAWalkViewModel::onSubmitWalking,
        modifier = Modifier.height(56.dp),
        enabled = surveyDialogState.picUri != null,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        contentPadding = contentPadding
    ) {
        Text(stringResource(id = R.string.submit))
    }
}

@Composable
private fun SurveyContent(
    modifier: Modifier = Modifier,
    logAWalkViewModel: LogAWalkViewModel,
    surveyDialogState: SurveyDialogState
) {
    val pickedUpLitterText = stringResource(R.string.picked_up_litter)
    val didNotPickUpLitterText = stringResource(R.string.did_not_pick_up_litter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.did_you_pick_up_litter))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = surveyDialogState.pickedUpLitter,
                    onClick = { logAWalkViewModel.onPickedUpLitterChange(true) },
                    modifier = Modifier
                        .semantics { contentDescription = pickedUpLitterText }
                )
                Text(text = stringResource(R.string.yes))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !surveyDialogState.pickedUpLitter,
                    onClick = { logAWalkViewModel.onPickedUpLitterChange(false) },
                    modifier = Modifier
                        .semantics { contentDescription = didNotPickUpLitterText }
                )
                Text(text = stringResource(R.string.No))
            }

        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            if (surveyDialogState.pickedUpLitter) {
                Spacer(modifier = Modifier.size(12.dp))
                Text(stringResource(R.string.how_many_bags_collected))
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    HorizontalNumberPicker(
                        minValue = 0,
                        maxValue = 10,
                        defaultValue = 0,
                        onValueChange = logAWalkViewModel::onBagsCollectedChange
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(stringResource(R.string.take_picture_of_litter))

        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            TakeAPic(
                logAWalkViewModel = logAWalkViewModel,
                surveyDialogState = surveyDialogState
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun TakeAPic(
    logAWalkViewModel: LogAWalkViewModel,
    surveyDialogState: SurveyDialogState
) {
    val context = LocalContext.current
    val permissionState =
        rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.CAMERA))

    if (surveyDialogState.showCamera) {
        FullScreen {
            var showPreview by rememberSaveable { mutableStateOf(false) }
            CameraCapture(onImageFile = logAWalkViewModel::onCapturePhoto, onDismissCamera = {
                if (!showPreview) {
                    logAWalkViewModel.onHideCamera()
                }
            })
            if (surveyDialogState.picUri != null) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(data = surveyDialogState.picUri)
                        .build(),
                    onSuccess = { showPreview = true }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (showPreview) 1f else 0f)
                ) {
                    Image(
                        painter = painter,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = null
                    )
                    FilledIconButton(
                        onClick = {
                            logAWalkViewModel.onDiscardPhoto()
                            showPreview = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 40.dp, vertical = 90.dp)
                            .size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(red = 0, green = 0, blue = 0, alpha = 0x2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Discard Image",
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    FilledIconButton(
                        onClick = logAWalkViewModel::onConfirmPhoto,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 40.dp, vertical = 90.dp)
                            .size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(red = 0, green = 0, blue = 0, alpha = 0x2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Confirm Image",
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
        }
    }

    if (permissionState.allPermissionsGranted) {
        if (surveyDialogState.picUri == null) {
            TextButton(
                onClick = logAWalkViewModel::onShowCamera,
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = stringResource(
                        R.string.take_picture_of_litter_description
                    ),
                    modifier = Modifier.size(48.dp),
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest
                            .Builder(LocalContext.current)
                            .data(data = surveyDialogState.picUri)
                            .build()
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentDescription = null
                )
                TextButton(onClick = logAWalkViewModel::onShowCamera) {
                    Text("Take another picture")
                }
            }
        }
    } else {
        RequestPermissions(
            permissionState = permissionState,
            askedOncePrefKey = R.string.wfr_asked_camera_permission_once,
            dontAskAgainPrefKey = R.string.wfr_dont_ask_camera_permissions,
            rationaleContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.size(12.dp))
                    WFRButton(
                        label = R.string.grant_permissions,
                        onClick = permissionState::launchMultiplePermissionRequest
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.camera_permission_rationale),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                        textAlign = TextAlign.Center
                    )
                }
            }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(12.dp))
                WFRButton(label = R.string.open_app_settings, onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    ContextCompat.startActivity(context, intent, null)
                })
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = stringResource(R.string.camera_permission_rationale),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.please_open_settings_to_grant_permissions),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}