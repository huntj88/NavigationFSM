//
//  TestOperations.swift
//  iosApp
//
//  Created by James Hunt on 11/16/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import shared

class SwiftFSMPlatformOperations: NFSMExposedIosFSMOperations {
    func showUI(proxy: NFSMUIProxy, input: Any?, completionHandler_ completionHandler: @escaping (NFSMExposedIosFSMOperationsExposedResult?, Error?) -> Void) {
        completionHandler(NFSMExposedIosFSMOperationsComplete(data: input) as NFSMExposedIosFSMOperationsExposedResult, nil)
        
        switch 1 {
        case 1:
            completionHandler(NFSMExposedIosFSMOperationsComplete(data: input) as NFSMExposedIosFSMOperationsExposedResult, nil)
        case 2:
            completionHandler(NFSMExposedIosFSMOperationsBack() as NFSMExposedIosFSMOperationsExposedResult, nil)
        default:
            completionHandler(NFSMExposedIosFSMOperationsError(error: KotlinThrowable(message: "haha")) as NFSMExposedIosFSMOperationsExposedResult, nil)
        }
    }
}

class SwiftPlatformDependencies: NFSMPlatformDependencies {
    func flowEnd() {
        print("flow ended")
    }
}

func test() {
    let blah = NFSMIosFSMOperations(exposed: SwiftFSMPlatformOperations())
    NFSMFSMManager.init().doInit(fsmOperations: blah, platformDependencies: SwiftPlatformDependencies())
}
