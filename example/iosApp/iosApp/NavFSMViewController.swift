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

final class NavFSMViewController: UINavigationController {
    
    override func popViewController(animated: Bool) -> UIViewController? {
        // TODO: does not really work
        let vc = topViewController as! Backable
        vc.back()
        return topViewController
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        NFSMFSMManager.init().doInit(
            fsmOperations: SwiftFSMPlatformOperations(),
            platformDependencies: SwiftPlatformDependencies(rootViewController: self)
        )
    }
}

