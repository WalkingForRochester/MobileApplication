package com.walkingforrochester.walkingforrochester.android.ui.composable.profile

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.roundDouble
import com.walkingforrochester.walkingforrochester.android.ui.PhoneNumberVisualTransformation
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.CommunityServiceCheckbox
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.ProfileViewModel

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    uiState: ProfileScreenState,
    profileViewModel: ProfileViewModel
) {
    Card(
        modifier = modifier.animateContentSize(),
        elevation = CardDefaults.cardElevation()
    ) {
        if (uiState.profileDataLoading) {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 144.dp, bottom = 144.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            EditableProfile(
                uiState = uiState,
                profileViewModel = profileViewModel
            )
            if (!uiState.editProfile) {
                val dividerColor = LocalContentColor.current.copy(alpha = .2f)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor
                )
                ProfileStats(
                    label = stringResource(id = R.string.distances),
                    previousStat = "${roundDouble(uiState.distanceToday)} mi",
                    overallStat = "${roundDouble(uiState.distanceOverall)} mi"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor
                )
                ProfileStats(
                    label = stringResource(id = R.string.durations),
                    previousStat = DateUtils.formatElapsedTime(uiState.durationToday / 1000),
                    overallStat = DateUtils.formatElapsedTime(uiState.durationOverall / 1000),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor
                )
                Text(
                    text = stringResource(id = R.string.profile_disclaimer),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun EditableProfile(
    modifier: Modifier = Modifier,
    uiState: ProfileScreenState,
    profileViewModel: ProfileViewModel
) {
    Box(modifier = modifier) {
        if (uiState.editProfile) {
            EditProfile(
                uiState = uiState,
                profileViewModel = profileViewModel
            )
        } else {
            ProfileDataAndActions(
                uiState = uiState,
                onEdit = profileViewModel::onEdit,
                onShare = profileViewModel::onShare
            )
        }
    }
}

@Composable
fun ProfileStats(
    label: String, previousStat: String, overallStat: String, modifier: Modifier = Modifier
) {
    val style = MaterialTheme.typography.bodyMedium
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(128.dp)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Last Walk:", style = style)
            Text(text = previousStat, style = style)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Overall:", style = style)
            Text(text = overallStat, style = style)
        }
    }
}

@Composable
fun ProfileDataAndActions(
    modifier: Modifier = Modifier,
    uiState: ProfileScreenState,
    onEdit: () -> Unit,
    onShare: () -> Intent
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProfilePic(
            profilePic = uiState.profilePic,
            modifier = Modifier.padding(16.dp),
        )
        Column {
            ProfileActions(
                enabled = uiState.accountId != 0L,
                onEdit = onEdit,
                onShare = onShare
            )
            ProfileInfo(
                accountId = uiState.accountId,
                email = uiState.email,
                phone = uiState.phone,
                nickname = uiState.nickname,
                communityService = uiState.communityService
            )
        }
    }
}

@Composable
fun EditProfile(
    modifier: Modifier = Modifier,
    uiState: ProfileScreenState,
    profileViewModel: ProfileViewModel
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditProfilePic(
            uiState = uiState,
            setLocalPhotoUri = profileViewModel::setLocalPhotoUri
        )
        EditProfileInfo(
            uiState = uiState,
            profileViewModel = profileViewModel
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            WFROutlinedButton(label = R.string.cancel, onClick = profileViewModel::onCancel)
            WFRButton(
                label = R.string.save,
                onClick = profileViewModel::onSave,
                loading = uiState.profileDataSaving
            )
        }
    }
}

@Composable
fun ProfilePic(
    profilePic: String,
    modifier: Modifier = Modifier,
) {
    if (profilePic.isEmpty()) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = stringResource(R.string.profile_pic),
            modifier = modifier.size(128.dp)
        )
    } else {
        AsyncImage(
            model = profilePic,
            contentDescription = stringResource(R.string.profile_pic),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(128.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun EditProfilePic(
    modifier: Modifier = Modifier,
    uiState: ProfileScreenState,
    setLocalPhotoUri: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        setLocalPhotoUri(uri)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.profilePic.isBlank()) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(R.string.profile_pic),
                modifier = modifier.size(192.dp)
            )
        } else {
            uiState.localProfilePicUri?.let {
                AsyncImage(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(data = it)
                        .build(),
                    contentDescription = stringResource(R.string.profile_pic),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(image = Icons.Filled.AccountCircle),
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                )
            }
            if (uiState.localProfilePicUri == null) {
                AsyncImage(
                    model = uiState.profilePic,
                    error = rememberVectorPainter(image = Icons.Filled.AccountCircle),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(R.string.profile_pic),
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                )
            }
        }
        if (uiState.tooLargeImage) {
            Text(
                text = stringResource(R.string.too_large_image),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        WFROutlinedButton(
            onClick = { launcher.launch("image/*") },
            label = R.string.choose_new_photo,
        )
    }
}

@Composable
fun ProfileActions(
    modifier: Modifier = Modifier, enabled: Boolean, onEdit: () -> Unit, onShare: () -> Intent
) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.End, modifier = modifier.fillMaxWidth()) {
        IconButton(onClick = onEdit, enabled = enabled) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_profile)
            )
        }
        IconButton(onClick = { context.startActivity(onShare()) }, enabled = enabled) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share_profile)
            )
        }
    }
}

@Composable
fun ProfileInfo(
    modifier: Modifier = Modifier,
    accountId: Long,
    email: String,
    phone: String,
    nickname: String,
    communityService: Boolean
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .padding(end = 16.dp, bottom = 16.dp)
    ) {
        val context = LocalContext.current
        val formattedPhone = remember(phone) {
            val transform = PhoneNumberVisualTransformation(context)
            transform.filter(AnnotatedString(phone)).text
        }

        val style = MaterialTheme.typography.bodyMedium
        Text(text = "AccountID: $accountId", style = style)
        if (nickname.isNotBlank()) {
            Text(text = nickname, style = style)
        }
        Text(text = email, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = formattedPhone, style = style)
        if (communityService) {
            Text("Community service: YES", style = style)
        }
    }
}

@Composable
fun EditProfileInfo(
    modifier: Modifier = Modifier,
    uiState: ProfileScreenState,
    profileViewModel: ProfileViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .wrapContentHeight()
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        WFROutlinedTextField(
            modifier = Modifier.padding(horizontal = 12.dp),
            value = uiState.email,
            onValueChange = profileViewModel::onEmailChange,
            labelRes = R.string.email_address,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = uiState.emailValidationMessage,
            clearFieldIconEnabled = true
        )
        WFROutlinedTextField(
            modifier = Modifier.padding(horizontal = 12.dp),
            value = uiState.phone,
            onValueChange = profileViewModel::onPhoneChange,
            labelRes = R.string.phone_number,
            visualTransformation = PhoneNumberVisualTransformation(LocalContext.current),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            validationError = uiState.phoneValidationMessage,
            clearFieldIconEnabled = true
        )
        WFROutlinedTextField(
            modifier = Modifier.padding(horizontal = 12.dp),
            value = uiState.nickname,
            onValueChange = profileViewModel::onNicknameChange,
            labelRes = R.string.nickname,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            clearFieldIconEnabled = true
        )
        CommunityServiceCheckbox(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 4.dp),
            checked = uiState.communityService,
            onCheckedChange = profileViewModel::onCommunityServiceChange,
            labelColor = MaterialTheme.colorScheme.onSurface,
            checkmarkColor = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun PreviewProfileCard() {
    WalkingForRochesterTheme {
        //ProfileCard()
    }
}