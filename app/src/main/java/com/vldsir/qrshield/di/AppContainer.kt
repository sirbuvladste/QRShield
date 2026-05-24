package com.vldsir.qrshield.di

import android.content.Context
import androidx.room.Room
import com.vldsir.qrshield.BuildConfig
import com.vldsir.qrshield.analysis.injection.InjectionDetector
import com.vldsir.qrshield.analysis.risk.RiskScoringEngine
import com.vldsir.qrshield.analysis.url.UrlHeuristicAnalyzer
import com.vldsir.qrshield.analysis.wifi.WifiAnalyzer
import com.vldsir.qrshield.analysis.wifi.WifiQrParser
import com.vldsir.qrshield.classifier.PayloadClassifier
import com.vldsir.qrshield.data.blocklist.DomainBlocklistLoader
import com.vldsir.qrshield.data.history.ScanDatabase
import com.vldsir.qrshield.data.history.ScanHistoryRepository
import com.vldsir.qrshield.data.network.NetworkAvailability
import com.vldsir.qrshield.data.network.ReputationRepository
import com.vldsir.qrshield.data.network.VirusTotalApiFactory
import com.vldsir.qrshield.data.preferences.SettingsRepository
import com.vldsir.qrshield.domain.IntentLauncher
import com.vldsir.qrshield.domain.ScanOrchestrator

class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)

    val database: ScanDatabase = Room.databaseBuilder(
        context,
        ScanDatabase::class.java,
        "qrshield.db",
    ).fallbackToDestructiveMigration().build()

    val historyRepository = ScanHistoryRepository(database.scanDao())
    val domainBlocklist = DomainBlocklistLoader(context)
    val urlAnalyzer = UrlHeuristicAnalyzer(domainBlocklist)
    val wifiParser = WifiQrParser()
    val wifiAnalyzer = WifiAnalyzer()
    val injectionDetector = InjectionDetector()
    val classifier = PayloadClassifier()
    val networkAvailability = NetworkAvailability(context)
    val reputationRepository = ReputationRepository(
        api = VirusTotalApiFactory.create(),
        apiKey = BuildConfig.VIRUSTOTAL_API_KEY,
    )
    val riskEngine = RiskScoringEngine()
    val orchestrator = ScanOrchestrator(
        classifier = classifier,
        urlAnalyzer = urlAnalyzer,
        wifiParser = wifiParser,
        wifiAnalyzer = wifiAnalyzer,
        injectionDetector = injectionDetector,
        reputationRepository = reputationRepository,
        networkAvailability = networkAvailability,
        riskEngine = riskEngine,
        historyRepository = historyRepository,
    )
    val intentLauncher = IntentLauncher(context)
}
