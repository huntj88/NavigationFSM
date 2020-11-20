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

//        let data = LoginNavFSMCredentials(username: "wow", password: "not")
//        proxy.completableDeferred.complete(value: ExposeAPIKt.complete(output: data))
    }
    
    
    
    
    
    
//    func showUI(proxy: NFSMUIProxy, input: Any?) -> Kotlinx_coroutines_coreDeferred {
//        completionHandler(NFSMExposedIosFSMOperationsComplete(data: input) as NFSMExposedIosFSMOperationsExposedResult, nil)
//
//        print("INPUT")
//        print(input)
//
//        (NFSMFSMManager.init().platformDependencies as! SwiftPlatformDependencies).rootViewController.pushViewController(TestViewController.storyboardInstance()!, animated: true)
//        let data = LoginNavFSMCredentials(username: "wow", password: "not")
//
//        switch 1 {
//        case 1:
//            proxy.completableDeferred.await(completionHandler: { output, error in ((Any?, Error?) -> Void).self
//
//                print("data")
//                print(output)
//                completionHandler(
//                    NFSMExposedIosFSMOperationsComplete(data: output) as NFSMExposedIosFSMOperationsExposedResult, error
//                )
//            })
//
//
//            proxy.completableDeferred.complete(value: data)
//
////            if let idd = input.self as? String, idd == "invalid credentials" {
////                print(KotlinUnit())
////                completionHandler(NFSMExposedIosFSMOperationsComplete(data: KotlinUnit()) as NFSMExposedIosFSMOperationsExposedResult, nil)
////            } else {
//
////            }
////            completionHandler(NFSMExposedIosFSMOperationsComplete(data: input) as NFSMExposedIosFSMOperationsExposedResult, nil)
//            // hard coded values for testing onAttemptLogin state
//
//        case 2:
//            completionHandler(NFSMExposedIosFSMOperationsBack() as NFSMExposedIosFSMOperationsExposedResult, nil)
//        default:
//            completionHandler(NFSMExposedIosFSMOperationsError(error: KotlinThrowable(message: "haha")) as NFSMExposedIosFSMOperationsExposedResult, nil)
//        }
//    }
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
