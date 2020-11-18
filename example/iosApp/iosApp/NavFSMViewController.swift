//
//  NavFSMViewController.swift
//  iosApp
//
//  Created by James Hunt on 11/16/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import UIKit
import SwiftUI
import shared

class IosDependencies: NFSMPlatformDependencies {
    func flowEnd() {
        print("FLOW ENDED")
    }
}

final class NavFSMViewController: UIViewController {
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    
        NFSMFSMManager.init().doInit(
            fsmOperations: NFSMIosFSMOperations(exposed: SwiftFSMPlatformOperations()),
            platformDependencies: IosDependencies()
        )
        
        self.add(asChildViewController: TestViewController.storyboardInstance()!)
    }

    private func add(asChildViewController viewController: UIViewController) {
        // Add Child View Controller
        addChild(viewController)

        // Add Child View as Subview
        view.addSubview(viewController.view)

        // Configure Child View
        viewController.view.frame = view.bounds
        viewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]

        // Notify Child View Controller
        viewController.didMove(toParent: self)
    }

}
