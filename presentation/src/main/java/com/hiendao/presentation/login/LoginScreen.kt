@file:Suppress("OPT_IN_IS_NOT_ENABLED")
package com.hiendao.presentation.login


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiendao.presentation.R
import com.hiendao.coreui.R as CoreR
import androidx.compose.ui.res.stringResource

// Brand colors
private val FacebookBlue = Color(0xFF1877F2)
private val GoogleBlue = Color(0xFF4285F4)
private val DividerGrey = Color(0xFFE5E7EB) // subtle border
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun LoginScreen(
    onFacebookClick: () -> Unit,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppLogoBox()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(CoreR.string.login_title_text),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(CoreR.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(28.dp))

                SocialButton(
                    text = stringResource(CoreR.string.continue_with_facebook),
                    borderColor = FacebookBlue.copy(alpha = 0.5f),
                    textColor = FacebookBlue,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_facebook),
                            contentDescription = stringResource(CoreR.string.content_desc_facebook),
                            tint = Color.Unspecified, // giữ màu gốc của icon
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    },
                    onClick = onFacebookClick
                )

                Spacer(Modifier.height(12.dp))

                SocialButton(
                    text = stringResource(CoreR.string.continue_with_google),
                    borderColor = DividerGrey,
                    textColor = Color(0xFF111827),
                    icon = {
                        Icon(
                        painter = painterResource(id = R.drawable.icon_google),
                        contentDescription = stringResource(CoreR.string.content_desc_google),
                        tint = Color.Unspecified, // giữ màu gốc của icon
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    },
                    onClick = onGoogleClick
                )
            }

            TermsAndPrivacy()
        }
    }
}

@Composable
internal fun AppLogoBox() {
    Image(
        painter = painterResource(id = R.drawable.icon_app),
        contentDescription = stringResource(CoreR.string.content_desc_app_icon),
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(32.dp))
    )
}

@Composable
private fun SocialButton(
    text: String,
    borderColor: Color,
    textColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = textColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) { icon() }

            Spacer(Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.1.sp
                ),
                color = textColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun TermsAndPrivacy() {
    val txt = buildAnnotatedString {
        withStyle(SpanStyle(color = TextSecondary)) {
            append(stringResource(CoreR.string.terms_agreement))
        }
        withStyle(SpanStyle(color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)) {
            append(stringResource(CoreR.string.terms_of_service))
        }
        withStyle(SpanStyle(color = TextSecondary)) {
            append(stringResource(CoreR.string.text_and))
        }
        withStyle(SpanStyle(color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)) {
            append(stringResource(CoreR.string.privacy_policy))
        }
    }
    Text(
        text = txt,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp,
        modifier = Modifier
            .padding(bottom = 24.dp)
            .fillMaxWidth(),
        color = TextSecondary
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoginPreview() {
    LoginScreen(
        onFacebookClick = {},
        onGoogleClick = {}
    )
}