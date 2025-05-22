package com.walkingforrochester.walkingforrochester.android.ui.composable.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.formatDouble
import com.walkingforrochester.walkingforrochester.android.formatElapsedMilli
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.ui.PhoneNumberVisualTransformation
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.CommunityServiceCheckbox
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun ProfileCard(
    uiState: ProfileScreenState,
    accountProfile: AccountProfile,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onProfileChange: (AccountProfile) -> Unit = {},
    onChoosePhoto: (Uri?) -> Unit = {},
    onSaveProfile: () -> Unit = {},
    onCancelEdits: () -> Unit = {}
) {
    Card(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        when {
            uiState.profileDataLoading -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 144.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            accountProfile.accountId == AccountProfile.NO_ACCOUNT -> {
                Text(
                    text = stringResource(R.string.profile_load_error),
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 144.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineBreak = LineBreak.Heading
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            else -> {
                EditableProfile(
                    uiState = uiState,
                    accountProfile = accountProfile,
                    modifier = Modifier,
                    onEdit = onEdit,
                    onShare = onShare,
                    onProfileChange = onProfileChange,
                    onChoosePhoto = onChoosePhoto,
                    onSaveProfile = onSaveProfile,
                    onCancelEdits = onCancelEdits
                )
                if (!uiState.editProfile) {
                    val dividerColor = LocalContentColor.current.copy(alpha = .2f)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = dividerColor
                    )
                    ProfileStats(
                        label = stringResource(id = R.string.distances),
                        previousStat = "${accountProfile.distanceToday.formatDouble()} mi",
                        overallStat = "${accountProfile.totalDistance.formatDouble()} mi"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = dividerColor
                    )
                    ProfileStats(
                        label = stringResource(id = R.string.durations),
                        previousStat = accountProfile.durationToday.formatElapsedMilli(),
                        overallStat = accountProfile.totalDuration.formatElapsedMilli(),
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
}

@Composable
fun EditableProfile(
    uiState: ProfileScreenState,
    accountProfile: AccountProfile,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onProfileChange: (AccountProfile) -> Unit = {},
    onChoosePhoto: (Uri?) -> Unit = {},
    onSaveProfile: () -> Unit = {},
    onCancelEdits: () -> Unit = {}
) {
    Box(modifier = modifier) {
        if (uiState.editProfile) {
            EditProfile(
                uiState = uiState,
                accountProfile = accountProfile,
                onProfileChange = onProfileChange,
                onChoosePhoto = onChoosePhoto,
                onSaveProfile = onSaveProfile,
                onCancelEdits = onCancelEdits
            )
        } else {
            ProfileDataAndActions(
                accountProfile = accountProfile,
                onEdit = onEdit,
                onShare = onShare
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
    accountProfile: AccountProfile,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        ProfilePic(
            profilePic = accountProfile.imageUrl,
            modifier = Modifier.padding(16.dp),
        )
        Column(modifier.padding(top = 4.dp, bottom = 16.dp)) {
            ProfileActions(
                enabled = accountProfile.accountId != 0L,
                onEdit = onEdit,
                onShare = onShare
            )
            ProfileInfo(
                accountId = accountProfile.accountId,
                email = accountProfile.email,
                phone = accountProfile.phoneNumber,
                nickname = accountProfile.nickname,
                communityService = accountProfile.communityService
            )
        }
    }
}

@Composable
fun EditProfile(
    uiState: ProfileScreenState,
    accountProfile: AccountProfile,
    modifier: Modifier = Modifier,
    onProfileChange: (AccountProfile) -> Unit = {},
    onChoosePhoto: (Uri?) -> Unit = {},
    onSaveProfile: () -> Unit = {},
    onCancelEdits: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditProfilePic(
            currentImageUrl = accountProfile.imageUrl,
            localImageUri = uiState.localProfilePicUri,
            modifier = Modifier,
            onChoosePhoto = onChoosePhoto
        )
        EditProfileInfo(
            accountProfile = accountProfile,
            emailValidationMessageId = uiState.emailValidationMessageId,
            phoneValidationMessageId = uiState.phoneValidationMessageId,
            onProfileChange = onProfileChange
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            WFROutlinedButton(
                label = R.string.cancel,
                onClick = onCancelEdits
            )
            WFRButton(
                label = R.string.save,
                onClick = onSaveProfile,
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
    currentImageUrl: String,
    localImageUri: Uri?,
    modifier: Modifier = Modifier,
    onChoosePhoto: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onChoosePhoto(uri)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            localImageUri != null -> {
                AsyncImage(
                    model = localImageUri,
                    contentDescription = stringResource(R.string.profile_pic),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(image = Icons.Filled.AccountCircle),
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                )
            }

            currentImageUrl.isBlank() -> {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.profile_pic),
                    modifier = modifier.size(192.dp)
                )
            }

            else -> {
                AsyncImage(
                    model = currentImageUrl,
                    error = rememberVectorPainter(image = Icons.Filled.AccountCircle),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(R.string.profile_pic),
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                )
            }
        }

        WFROutlinedButton(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            label = R.string.choose_new_photo,
        )
    }
}

@Composable
fun ProfileActions(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(end = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onEdit, enabled = enabled) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_profile)
            )
        }
        IconButton(onClick = onShare, enabled = enabled) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share_profile)
            )
        }
    }
}

@Composable
fun ProfileInfo(
    accountId: Long,
    email: String,
    phone: String,
    nickname: String,
    communityService: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .padding(end = 16.dp)
            .fillMaxWidth()
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
    accountProfile: AccountProfile,
    emailValidationMessageId: Int,
    phoneValidationMessageId: Int,
    modifier: Modifier = Modifier,
    onProfileChange: (AccountProfile) -> Unit = {}
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
            value = accountProfile.email,
            onValueChange = { newEmail ->
                onProfileChange(accountProfile.copy(email = newEmail))
            },
            labelRes = R.string.email_address,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(emailValidationMessageId)
        )
        WFROutlinedTextField(
            modifier = Modifier.padding(horizontal = 12.dp),
            value = accountProfile.phoneNumber,
            onValueChange = { newPhone ->
                onProfileChange(accountProfile.copy(phoneNumber = newPhone))
            },
            labelRes = R.string.phone_number,
            visualTransformation = PhoneNumberVisualTransformation(LocalContext.current),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(phoneValidationMessageId)
        )
        WFROutlinedTextField(
            modifier = Modifier.padding(horizontal = 12.dp),
            value = accountProfile.nickname,
            onValueChange = { newNickName ->
                onProfileChange(accountProfile.copy(nickname = newNickName))
            },
            labelRes = R.string.nickname,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
        CommunityServiceCheckbox(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 4.dp),
            checked = accountProfile.communityService,
            onCheckedChange = { newValue ->
                onProfileChange(accountProfile.copy(communityService = newValue))
            },
            labelColor = MaterialTheme.colorScheme.onSurface,
            checkmarkColor = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

@Composable
private fun errorMessage(@StringRes msgId: Int): String {
    return when {
        msgId != 0 -> stringResource(id = msgId)
        else -> ""
    }
}

@Preview
@Composable
fun PreviewProfileCard() {
    WalkingForRochesterTheme {
        ProfileCard(
            uiState = ProfileScreenState(),
            accountProfile = AccountProfile.DEFAULT_PROFILE.copy(
                accountId = 1234L,
                email = "test@email.com",
                phoneNumber = "5551234567",
                nickname = "Bob",
                communityService = true,
            )
        )
    }
}

@Preview
@Composable
fun PreviewNoAccount() {
    WalkingForRochesterTheme {
        ProfileCard(
            uiState = ProfileScreenState(),
            accountProfile = AccountProfile.DEFAULT_PROFILE
        )
    }
}

@Preview
@Composable
fun PreviewProfileCardEditing() {
    WalkingForRochesterTheme {
        ProfileCard(
            uiState = ProfileScreenState(editProfile = true),
            accountProfile = AccountProfile.DEFAULT_PROFILE.copy(
                accountId = 1234L,
                email = "test@email.com",
                phoneNumber = "5551234567",
                nickname = "Bob",
                communityService = true,
            )
        )
    }
}
