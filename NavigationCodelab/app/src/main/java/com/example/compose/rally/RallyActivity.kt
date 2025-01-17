/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.rally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.compose.rally.data.UserData
import com.example.compose.rally.ui.accounts.AccountsBody
import com.example.compose.rally.ui.accounts.SingleAccountBody
import com.example.compose.rally.ui.bills.BillsBody
import com.example.compose.rally.ui.bills.SingleBillBody
import com.example.compose.rally.ui.components.RallyTabRow
import com.example.compose.rally.ui.overview.OverviewBody
import com.example.compose.rally.ui.theme.RallyTheme

/**
 * This Activity recreates part of the Rally Material Study from
 * https://material.io/design/material-studies/rally.html
 */
class RallyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RallyApp()
        }
    }
}

@Composable
fun RallyApp() {
    RallyTheme {
        val allScreens = RallyScreen.values().toList()
        val navController = rememberNavController()
        val backstackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = RallyScreen.fromRoute(backstackEntry.value?.destination?.route)
        Scaffold(
            topBar = {
                RallyTabRow(
                    allScreens = allScreens,
                    onTabSelected = { screen ->
                        navController.navigate(screen.name)
                    },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            RallyNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun RallyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    // https://stackoverflow.com/questions/68962458/how-are-android-activities-handled-with-jetpack-compose-and-compose-navigation
    // The Compose application is designed to be used in a single-activity architecture with no fragments.
    // This suggests that we only have one activity, and switch out composables for "screens".
    // rally://accounts/Checking -> goes to rally app, accounts screen, with Checking name
    NavHost(
        navController = navController,
        startDestination = RallyScreen.Overview.name,
        modifier = modifier
    ) {

        val onAccountClick: (String) -> Unit = { name ->
            navigateToSingleAccount(
                navController = navController,
                accountName = name
            )
        }

        val onBillClick: (String) -> Unit = { name ->
            navigateToSingleBill(
                navController = navController,
                billName = name
            )
        }

        composable(RallyScreen.Overview.name) {
            OverviewBody(
                onClickSeeAllAccounts = { navController.navigate(RallyScreen.Accounts.name) },
                onClickSeeAllBills = { navController.navigate(RallyScreen.Bills.name) },
                onAccountClick = onAccountClick,
                onBillClick = onBillClick
            )
        }

        val accountsName = RallyScreen.Accounts.name
        composable(accountsName) {
            AccountsBody(UserData.accounts, onAccountClick = onAccountClick)
        }
        composable(
            "$accountsName/{name}",
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType // Make argument type safe
                }
            ),
            // Test deeplink: adb shell am start -d "rally://accounts/Checking" -a android.intent.action.VIEW
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "rally://$accountsName/{name}"
                }
            )
        ) { entry ->
            val accountName = entry.arguments?.getString("name")
            val account = UserData.getAccount(accountName)
            SingleAccountBody(account = account)
        }

        val billsName = RallyScreen.Bills.name
        composable(billsName) {
            BillsBody(UserData.bills, onBillClick = onBillClick)
        }

        composable(
            "$billsName/{name}",
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType // Make argument type safe
                }
            ),
            // Test deeplink: adb shell am start -d "rally://accounts/Checking" -a android.intent.action.VIEW
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "rally://$billsName/{name}"
                }
            )
        ) { entry ->
            val billName = entry.arguments?.getString("name")
            val bill = UserData.getBillByName(billName)
            SingleBillBody(bill = bill)
        }
    }
}

private fun navigateToSingleAccount(
    navController: NavHostController,
    accountName: String
) {
    navController.navigate("${RallyScreen.Accounts.name}/$accountName")
}

private fun navigateToSingleBill(
    navController: NavHostController,
    billName: String
) {
    navController.navigate("${RallyScreen.Bills.name}/$billName")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RallyTheme {
        RallyApp()
    }
}
