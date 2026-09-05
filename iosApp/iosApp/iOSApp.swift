import UIKit
import ComposeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        print("[OSINT_BOOT] AppDelegate didFinishLaunchingWithOptions")
        let win = UIWindow(frame: UIScreen.main.bounds)
        self.window = win
        win.rootViewController = MainViewControllerKt.MainViewController()
        win.makeKeyAndVisible()
        print("[OSINT_BOOT] Window made key and visible")
        return true
    }
}
