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


class SetLockPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppLockSet()
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
fun AppLockSet() {
    val passwordInput = rememberSaveable {
        mutableStateOf("")
    }
    val passwordRepeat = rememberSaveable {
        mutableStateOf("")
    }

    val isPasswordInputError = rememberSaveable {
        mutableStateOf(false)
    }
    val isPasswordRepeatError = rememberSaveable {
        mutableStateOf(false)
    }

    AppLockTheme {
        // A surface container using the 'background' color from the theme
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
                    PasswordTextField(
                        passwordInput, passwordRepeat, isPasswordInputError, isPasswordRepeatError
                    )
                    PasswordRepeatField(passwordInput, passwordRepeat, isPasswordRepeatError)
                    ConfigButtons(passwordInput, passwordRepeat, isPasswordRepeatError)
                }
            }

        }
    }
}


@Composable
fun ConfigButtons(
    passwordInput: MutableState<String>,
    passwordRepeat: MutableState<String>,
    isPasswordRepeatError: MutableState<Boolean>
) {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "com.sunritel.applock",
        MODE_PRIVATE
    )
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
                LockUtil.log("passwordInput: ${passwordInput.value}")
                LockUtil.log("passwordRepeat: ${passwordRepeat.value}")
                if (passwordInput.value == passwordRepeat.value) {
                    val encodedPassword = LockUtil.sha256(passwordInput.value)
                    LockUtil.log("Password: $encodedPassword")
                    sharedPreferences.edit().putString("password", encodedPassword).apply()
                    sharedPreferences.edit().putBoolean("isPasswordSet", true).apply()
                    Toast.makeText(context, "Password saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent()
                    intent.putExtra("result", true)
                    activity?.setResult(RESULT_OK, intent)
                    activity?.finish()
                } else {
                    isPasswordRepeatError.value = true
                    Toast.makeText(context, "Password not match", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text(text = "Save")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    passwordInput: MutableState<String>,
    passwordRepeat: MutableState<String>,
    isPasswordInputError: MutableState<Boolean>,
    isPasswordRepeatError: MutableState<Boolean>
) {
    val errorMessage = "Password too long"
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    val charLimiterMin = 3
    val charLimiterMax = 20
    fun validatePassword() {
        isPasswordInputError.value =
            passwordInput.value.length > charLimiterMax || passwordInput.value.length < charLimiterMin
        isPasswordRepeatError.value = passwordInput.value != passwordRepeat.value
    }
    TextField(
        value = passwordInput.value,
        onValueChange = {
            passwordInput.value = it
            validatePassword()
        },
        singleLine = true,
        label = { Text(if (isPasswordInputError.value) "Password*" else "Password") },
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
        isError = isPasswordInputError.value,
        supportingText = {
            Text(
                modifier = Modifier.fillMaxWidth(1f), text = if (isPasswordInputError.value) {
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
                if (isPasswordInputError.value) error(errorMessage)
            },
        keyboardActions = KeyboardActions {
            validatePassword()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRepeatField(
    passwordInput: MutableState<String>,
    passwordRepeat: MutableState<String>,
    isError: MutableState<Boolean>
) {
    val errorMessage = "Password does not match"
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    val charLimiterMax = 20
    fun validatePassword() {
        isError.value = passwordInput.value != passwordRepeat.value
    }
    TextField(
        value = passwordRepeat.value,
        onValueChange = {
            passwordRepeat.value = it
            validatePassword()
        },
        singleLine = true,
        label = { Text(if (isError.value) "Repeat Password*" else "Repeat Password") },
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
        placeholder = { Text("Please repeat your password") },
        isError = isError.value,
        supportingText = {
            Text(
                modifier = Modifier.fillMaxWidth(1f),
                text = if (isError.value) errorMessage else "Limit ${passwordRepeat.value.length}/$charLimiterMax",
                textAlign = TextAlign.End
            )
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(1f)
            .semantics {
                if (isError.value) error(errorMessage)
            },
        keyboardActions = KeyboardActions {
            validatePassword()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Preview
@Composable
fun AppLockSetPreview() {
    AppLockSet()
}