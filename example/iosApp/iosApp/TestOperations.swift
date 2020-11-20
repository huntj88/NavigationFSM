//
//  TestOperations.swift
//  iosApp
//
//  Created by James Hunt on 11/16/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import shared
import UIKit

class SwiftFSMPlatformOperations: NFSMPlatformFSMOperations {
    func duplicate() -> NFSMPlatformFSMOperations {
        // TODO
        return self
    }
    
    func showUI(proxy: NFSMUIProxy, input: Any?, completionHandler: @escaping (NFSMFSMResult<AnyObject>?, Error?) -> Void) {
        (NFSMFSMManager.init().platformDependencies as! SwiftPlatformDependencies)
            .rootViewController
            .pushViewController(TestViewController.storyboardInstance(), animated: true)
        
        proxy.completableDeferred.await(completionHandler: { output, error in ((NFSMFSMResult<AnyObject>, Error?) -> Void).self
            completionHandler(output as! NFSMFSMResult<AnyObject>, error)
        })
    }
}

class SwiftPlatformDependencies: NFSMPlatformDependencies {
    let rootViewController: UINavigationController
    init(rootViewController: UINavigationController) {
        self.rootViewController = rootViewController
    }
    
    func flowEnd() {
        print("flow ended")
    }
}
