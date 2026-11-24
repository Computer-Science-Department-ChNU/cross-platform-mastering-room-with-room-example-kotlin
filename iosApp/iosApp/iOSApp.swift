import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        Koin_iosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}