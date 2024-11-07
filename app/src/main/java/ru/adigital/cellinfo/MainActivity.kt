package ru.adigital.cellinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.TELEPHONY_SERVICE
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.adigital.cellinfo.ui.theme.CellInfoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                ), 0
            )
        } else {
            setContent {
                CellInfoTheme {
                    SignalInfoScreen()
                }
            }
        }

    }
}

@SuppressLint("MissingPermission")
@Composable
fun SignalInfoScreen() {
    val context = LocalContext.current
    val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

    // Используем состояние для хранения списка параметров сигнала
    var signalInfoWithBackground by remember {
        mutableStateOf(getSignalInfoList(telephonyManager.allCellInfo))
    }

    var navigateToDetails by remember { mutableStateOf(false) }

    // Функция обновления данных сигнала
    fun refreshSignalInfo() {
        signalInfoWithBackground = getSignalInfoList(telephonyManager.allCellInfo)
    }

    if (navigateToDetails) {
        TelephonyDetailsScreen(onBackPressed = { navigateToDetails = false })
    } else {
        // Отображение LazyColumn с кнопкой обновления FAB
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(signalInfoWithBackground) { (infoList, backgroundColor) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(8.dp)
                    ) {
                        infoList.forEach { info ->
                            Text(
                                text = "${info.first}: ${info.second}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            thickness = 0.5.dp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // FloatingActionButton для перехода на экран с подробной информацией
            FloatingActionButton(
                onClick = { navigateToDetails = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Go to Details Screen")
            }

            // FloatingActionButton для обновления данных
            FloatingActionButton(
                onClick = { refreshSignalInfo() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh Signal Info")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun TelephonyDetailsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Telephony Manager Details",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .weight(1f)
            )

            // Кнопка "Назад" для возвращения на основной экран
            Button(onClick = { onBackPressed() }) {
                Text(text = "Back")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Показать полезную информацию о TelephonyManager
            Text("Network Operator: ${telephonyManager.networkOperatorName ?: "Unknown"}")
            Text("Network Operator Code: ${telephonyManager.networkOperator ?: "Unknown"}")
            Text("Network Country: ${telephonyManager.networkCountryIso ?: "Unknown"}")
            Text("Phone Type: ${getPhoneTypeName(telephonyManager.phoneType)}")
            Text("SIM Operator Name: ${telephonyManager.simOperatorName ?: "Unknown"}")
            Text("SIM Country ISO: ${telephonyManager.simCountryIso ?: "Unknown"}")
            Text("SIM State: ${getSimStateName(telephonyManager.simState)}")

            Spacer(modifier = Modifier.height(16.dp))

            // Мобильная сеть
            Text("Network Type: ${getNetworkTypeName(telephonyManager.networkType)}")
            Text("Data Network Type: ${getDataNetworkTypeName(telephonyManager.dataNetworkType)}")
            Text("Voice Network Type: ${getVoiceNetworkTypeName(telephonyManager.voiceNetworkType)}")
            Text("Is Network Roaming: ${telephonyManager.isNetworkRoaming}")
            Text("Is Data Enabled: ${telephonyManager.isDataEnabled}")
            Text("Is Data Roaming Enabled: ${telephonyManager.isDataRoamingEnabled}")

            Spacer(modifier = Modifier.height(16.dp))

            // Cell Info and Other Modem Details
            Text("Modem Enabled for Slot: ${telephonyManager.isModemEnabledForSlot(0)}")
            Text("Active Modem Count: ${telephonyManager.activeModemCount}")
            Text("Supported Modem Count: ${telephonyManager.supportedModemCount}")
            Text("Is Multi-Sim Supported: ${telephonyManager.isMultiSimSupported}")

            Spacer(modifier = Modifier.height(16.dp))

            // Дополнительная информация о сети
            Text("Is Voice Capable: ${telephonyManager.isVoiceCapable}")
            Text("Is Sms Capable: ${telephonyManager.isSmsCapable}")
            Text("Has ICC Card: ${telephonyManager.hasIccCard()}")
            Text("Service State: ${getServiceStateName(telephonyManager.serviceState)}")
            Text("Emergency Numbers Supported: ${telephonyManager.emergencyNumberList}")

        }
    }
}

// Функция для получения строкового представления типа телефона
fun getPhoneTypeName(phoneType: Int): String {
    return when (phoneType) {
        TelephonyManager.PHONE_TYPE_GSM -> "GSM"
        TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
        TelephonyManager.PHONE_TYPE_SIP -> "SIP"
        TelephonyManager.PHONE_TYPE_NONE -> "None"
        else -> "Unknown"
    }
}

// Функции для получения текста состояния SIM-карты
fun getSimStateName(simState: Int): String {
    return when (simState) {
        TelephonyManager.SIM_STATE_ABSENT -> "Absent"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
        TelephonyManager.SIM_STATE_READY -> "Ready"
        TelephonyManager.SIM_STATE_UNKNOWN -> "Unknown"
        else -> "Unknown"
    }
}

// Функция для получения текстового представления типа сети
fun getNetworkTypeName(networkType: Int): String {
    return when (networkType) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP"
        TelephonyManager.NETWORK_TYPE_NR -> "NR"
        else -> "Unknown"
    }
}

// Функция для получения текстового представления типа сети передачи данных
fun getDataNetworkTypeName(dataNetworkType: Int): String {
    return when (dataNetworkType) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        else -> "Unknown"
    }
}

// Функция для получения текстового представления типа голосовой сети
fun getVoiceNetworkTypeName(voiceNetworkType: Int): String {
    return when (voiceNetworkType) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        else -> "Unknown"
    }
}

// Функция для получения состояния сети
fun getServiceStateName(serviceState: ServiceState?): String {
    return when (serviceState?.state) {
        ServiceState.STATE_IN_SERVICE -> "In Service"
        ServiceState.STATE_OUT_OF_SERVICE -> "Out of Service"
        ServiceState.STATE_EMERGENCY_ONLY -> "Emergency Only"
        ServiceState.STATE_POWER_OFF -> "Power Off"
        else -> "Unknown"
    }
}

// Функция для получения списка сигналов и установки фона
fun getSignalInfoList(cellInfoList: List<CellInfo>): List<Pair<List<Pair<String, String>>, Color>> {
    return cellInfoList.map { cellInfo ->
        when (cellInfo) {
            is CellInfoGsm -> getGsmInfo(cellInfo) to Color(0xFFE3F2FD) // Голубой фон для GSM
            is CellInfoWcdma -> getWcdmaInfo(cellInfo) to Color(0xFFE8F5E9) // Зеленый фон для WCDMA
            is CellInfoLte -> getLteInfo(cellInfo) to Color(0xFFFFF3E0) // Оранжевый фон для LTE
            else -> emptyList<Pair<String, String>>() to Color.Transparent
        }
    }
}

// Функции для извлечения параметров сети по поколениям
fun getGsmInfo(cellInfo: CellInfoGsm): List<Pair<String, String>> {
    val cellIdentity = cellInfo.cellIdentity
    val cellSignalStrength = cellInfo.cellSignalStrength

    // Собираем информацию о GSM
    val infoList = mutableListOf<Pair<String, String>>()

    // Информация из CellIdentityGsm
    infoList.add("Type" to "2G (GSM)")
    infoList.add("CID" to cellIdentity.cid.toString())  // Cell ID
    infoList.add("LAC" to cellIdentity.lac.toString())  // Location Area Code
    infoList.add("MCC" to (cellIdentity.mccString ?: "Unavailable"))  // Mobile Country Code
    infoList.add("MNC" to (cellIdentity.mncString ?: "Unavailable"))  // Mobile Network Code
    infoList.add("ARFCN" to cellIdentity.arfcn.toString())  // Absolute Radio Frequency Channel Number
    infoList.add("BSIC" to cellIdentity.bsic.toString())  // Base Station Identity Code
    infoList.add(
        "Mobile Network Operator" to (cellIdentity.mobileNetworkOperator ?: "Unavailable")
    )  // Mobile Network Operator
    infoList.add("Additional PLMNs" to cellIdentity.additionalPlmns.joinToString(", "))  // Additional PLMN (Public Land Mobile Network)

    // Информация из CellSignalStrengthGsm
    infoList.add("Signal Strength (dBm)" to cellSignalStrength.dbm.toString())  // Signal strength in dBm
    infoList.add("Level" to cellSignalStrength.level.toString())  // Signal strength level
    infoList.add("Timing Advance" to cellSignalStrength.timingAdvance.toString())  // Timing advance value
    infoList.add("ASU Level" to cellSignalStrength.asuLevel.toString())  // ASU level
    infoList.add("RSSI" to cellSignalStrength.rssi.toString())  // Received Signal Strength Indicator
    infoList.add("Bit Error Rate" to cellSignalStrength.bitErrorRate.toString())  // Bit error rate

    // Информация из CellInfo (базовый класс)
    infoList.add("Is Registered" to cellInfo.isRegistered.toString())  // Whether the device is registered with the cell
    infoList.add(
        "Connection Status" to when (cellInfo.cellConnectionStatus) {
            CellInfo.CONNECTION_PRIMARY_SERVING -> "Primary Serving"
            CellInfo.CONNECTION_SECONDARY_SERVING -> "Secondary Serving"
            CellInfo.CONNECTION_NONE -> "No Connection"
            CellInfo.CONNECTION_UNKNOWN -> "Unknown"
            else -> "Unavailable"
        }
    )

    infoList.add("Timestamp (ms)" to cellInfo.timestampMillis.toString())

    // Возвращаем список с данными
    return infoList
}


fun getWcdmaInfo(cellInfo: CellInfoWcdma): List<Pair<String, String>> {
    val cellIdentity = cellInfo.cellIdentity
    val cellSignalStrength = cellInfo.cellSignalStrength

    // Собираем информацию о WCDMA
    val infoList = mutableListOf<Pair<String, String>>()

    // Информация из CellIdentityWcdma
    infoList.add("Type" to "3G (WCDMA)")
    infoList.add("CID" to cellIdentity.cid.toString())  // Cell ID
    infoList.add("LAC" to cellIdentity.lac.toString())  // Location Area Code
    infoList.add("MCC" to (cellIdentity.mccString ?: "Unavailable"))  // Mobile Country Code
    infoList.add("MNC" to (cellIdentity.mncString ?: "Unavailable"))  // Mobile Network Code
    infoList.add("UARFCN" to cellIdentity.uarfcn.toString())  // UTRA Absolute Radio Frequency Channel Number
    infoList.add("PSC" to cellIdentity.psc.toString())  // Primary Scrambling Code
    infoList.add(
        "Mobile Network Operator" to (cellIdentity.mobileNetworkOperator ?: "Unavailable")
    )  // Mobile Network Operator
    infoList.add(
        "Additional PLMNs" to cellIdentity.additionalPlmns.joinToString(", ")
    )  // Additional PLMN (Public Land Mobile Network)

    // Информация из CellSignalStrengthWcdma
    infoList.add("Signal Strength (dBm)" to cellSignalStrength.dbm.toString())  // Signal strength in dBm
    infoList.add("ASU Level" to cellSignalStrength.asuLevel.toString())  // ASU level
    infoList.add("EC/NO" to cellSignalStrength.ecNo.toString())  // Ec/No (Energy per chip over Noise)

    // Информация из CellInfo (базовый класс)
    infoList.add(
        "Is Registered" to cellInfo.isRegistered.toString()
    )  // Whether the device is registered with the cell
    infoList.add(
        "Connection Status" to when (cellInfo.cellConnectionStatus) {
            CellInfo.CONNECTION_PRIMARY_SERVING -> "Primary Serving"
            CellInfo.CONNECTION_SECONDARY_SERVING -> "Secondary Serving"
            CellInfo.CONNECTION_NONE -> "No Connection"
            CellInfo.CONNECTION_UNKNOWN -> "Unknown"
            else -> "Unavailable"
        }
    )

    infoList.add("Timestamp (ms)" to cellInfo.timestampMillis.toString())

    return infoList
}


fun getLteInfo(cellInfo: CellInfoLte): List<Pair<String, String>> {
    val cellIdentity = cellInfo.cellIdentity
    val cellSignalStrength = cellInfo.cellSignalStrength

    val infoList = mutableListOf(
        "Type" to "4G (LTE)",
        "ECI" to cellIdentity.ci.toString(),
        "PCI" to cellIdentity.pci.toString(),
        "TAC" to cellIdentity.tac.toString(),
        "MCC" to (cellIdentity.mccString ?: "Unavailable"),
        "MNC" to (cellIdentity.mncString ?: "Unavailable"),
        "Mobile Network Operator" to (cellIdentity.mobileNetworkOperator ?: "Unavailable"),
        "EARFCN" to cellIdentity.earfcn.toString(),
        "Bandwidth (kHz)" to cellIdentity.bandwidth.toString(),
        "Additional PLMNs" to cellIdentity.additionalPlmns.joinToString(", ") { it },
        "Bands" to cellIdentity.bands.joinToString(", "),
        "Closed Subscriber Group" to (cellIdentity.closedSubscriberGroupInfo?.toString()
            ?: "Not available"),
        "RSRP" to cellSignalStrength.rsrp.toString(),
        "RSRQ" to cellSignalStrength.rsrq.toString(),
        "RSSNR" to cellSignalStrength.rssnr.toString(),
        "CQI" to cellSignalStrength.cqi.toString(),
        "CQI Table Index" to cellSignalStrength.cqiTableIndex.toString(),
        "Signal Strength (dBm)" to cellSignalStrength.dbm.toString(),
        "ASU Level" to cellSignalStrength.asuLevel.toString(),
        "RSSI" to cellSignalStrength.rssi.toString(),
        "Timing Advance" to cellSignalStrength.timingAdvance.toString(),
        "Signal Level" to cellSignalStrength.level.toString(),
        "Connection Status" to when (cellInfo.cellConnectionStatus) {
            CellInfo.CONNECTION_PRIMARY_SERVING -> "Primary Serving"
            CellInfo.CONNECTION_SECONDARY_SERVING -> "Secondary Serving"
            CellInfo.CONNECTION_NONE -> "No Connection"
            CellInfo.CONNECTION_UNKNOWN -> "Unknown"
            else -> "Unavailable"
        },
        "Is Registered" to cellInfo.isRegistered.toString(),
        "Timestamp (ms)" to cellInfo.timestampMillis.toString()
    )

    Log.d("TEST", "getLteInfo: $infoList")

    return infoList
}