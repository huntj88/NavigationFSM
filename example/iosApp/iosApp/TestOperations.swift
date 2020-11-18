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

class SwiftFSMPlatformOperations: NFSMExposedIosFSMOperations {
    func showUI(proxy: NFSMUIProxy, input: Any?, completionHandler_ completionHandler: @escaping (NFSMExposedIosFSMOperationsExposedResult?, Error?) -> Void) {
        completionHandler(NFSMExposedIosFSMOperationsComplete(data: input) as NFSMExposedIosFSMOperationsExposedResult, nil)
        
        (NFSMFSMManager.init().platformDependencies as! SwiftPlatformDependencies).rootViewController.pushViewController(TestViewController.storyboardInstance()!, animated: true)
        
        switch 1 {
        case 1:
//            completionHandler(NFSMExposedIosFSMOperationsComplete(data: input) as NFSMExposedIosFSMOperationsExposedResult, nil)
            // hard coded values for testing onAttemptLogin state
            completionHandler(NFSMExposedIosFSMOperationsComplete(data: LoginNavFSMCredentials(username: "wow", password: "not wow")) as NFSMExposedIosFSMOperationsExposedResult, nil)
        case 2:
            completionHandler(NFSMExposedIosFSMOperationsBack() as NFSMExposedIosFSMOperationsExposedResult, nil)
        default:
            completionHandler(NFSMExposedIosFSMOperationsError(error: KotlinThrowable(message: "haha")) as NFSMExposedIosFSMOperationsExposedResult, nil)
        }
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

//func test() {
//    let blah = NFSMIosFSMOperations(exposed: SwiftFSMPlatformOperations())
//    NFSMFSMManager.init().doInit(fsmOperations: blah, platformDependencies: SwiftPlatformDependencies())
//    
//    
//    NFSMFSMManager.init().root.walkTreeForOperation(operation: { operations in
//        print((operations as! NFSMIosFSMOperations).description)
//    })
//}
