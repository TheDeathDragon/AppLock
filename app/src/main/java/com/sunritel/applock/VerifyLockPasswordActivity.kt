package com.sunritel.applock

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sunritel.applock.ui.theme.AppLockTheme


class VerifyLockPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "com.sunritel.applock", MODE_PRIVATE
        )
        LockUtil.log("isPasswordSet: ${sharedPreferences.getBoolean("isPasswordSet", false)}")
        if (!sharedPreferences.getBoolean("isPasswordSet", false)) {
            Toast.makeText(this, "Please set password first", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SetLockPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContent {
            AppLockVerify()
        }
    }

    override fun onBackPressed() {
        LockUtil.log("onBackPressed")
        val intent = Intent()
        intent.putExtra("result", false)
        setResult(RESULT_OK, intent)
        finish()
    }
}

@Composable
fun AppLockVerify() {
    val passwordInput = rememberSaveable {
        mutableStateOf("")
    }

    val isPasswordVerifyError = rememberSaveable {
        mutableStateOf(false)
    }

    AppLockTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    VerifyPasswordTextField(
                        passwordInput, isPasswordVerifyError
                    )
                    ConfigButtons(passwordInput, isPasswordVerifyError)
                }
            }

        }
    }
}


@Composable
fun ConfigButtons(
    passwordInput: MutableState<String>, isPasswordVerifyError: MutableState<Boolean>
) {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "com.sunritel.applock", MODE_PRIVATE
    )
    var savedPassword: String?
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        ElevatedButton(
            onClick = {
                focusManager.clearFocus()
                val intent = Intent()
                intent.putExtra("result", false)
                activity?.setResult(RESULT_OK, intent)
                activity?.finish()
            }, modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text(text = "Cancel")
        }
        ElevatedButton(
            onClick = {
                focusManager.clearFocus()
                savedPassword = sharedPreferences.getString("password", "")
                LockUtil.log("Password: $savedPassword")
                if (LockUtil.sha256(passwordInput.value) == savedPassword) {
                    Toast.makeText(context, "Password verified", Toast.LENGTH_SHORT).show()
                    val intent = Intent()
                    intent.putExtra("result", true)
                    activity?.setResult(RESULT_OK, intent)
                    activity?.finish()
                } else {
                    isPasswordVerifyError.value = true
                    passwordInput.value = ""
                    Toast.makeText(context, "Password not match", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text(text = "Verify")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPasswordTextField(
    passwordInput: MutableState<String>, isPasswordVerifyError: MutableState<Boolean>
) {
    val errorMessage = "Password too long"
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    val charLimiterMin = 3
    val charLimiterMax = 20
    fun validatePassword() {
        isPasswordVerifyError.value =
            passwordInput.value.length > charLimiterMax || passwordInput.value.length < charLimiterMin
    }
    TextField(
        value = passwordInput.value,
        onValueChange = {
            passwordInput.value = it
            validatePassword()
        },
        singleLine = true,
        label = { Text(if (isPasswordVerifyError.value) "Password*" else "Password") },
        visualTransformation = if (passwordHidden) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            IconButton(onClick = {
                passwordHidden = !passwordHidden
            }) {
                val visibilityIcon = if (passwordHidden) Icons.Filled.Visibility
                else Icons.Outlined.VisibilityOff
                val description = if (passwordHidden) "Show password"
                else "Hide password"
                Icon(visibilityIcon, contentDescription = description)
            }
        },
        placeholder = { Text("Please input your password") },
        isError = isPasswordVerifyError.value,
        supportingText = {
            Text(
                modifier = Modifier.fillMaxWidth(1f), text = if (isPasswordVerifyError.value) {
                    if (passwordInput.value.length < charLimiterMin) "Password length must be at least $charLimiterMin"
                    else "Password length must between $charLimiterMin and $charLimiterMax"
                } else {
                    "Limit ${passwordInput.value.length}/$charLimiterMax"
                }, textAlign = TextAlign.End
            )
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(1f)
            .semantics {
                if (isPasswordVerifyError.value) error(errorMessage)
            },
        keyboardActions = KeyboardActions {
            validatePassword()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Preview
@Composable
fun AppLockVerifyPreview() {
    AppLockVerify()
}