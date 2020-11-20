//
//  LoginUIProxyImpl.swift
//  iosApp
//
//  Created by James Hunt on 11/16/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import shared

class LoginUIProxyImpl: LoginUIProxy {
    var input: Any? = nil
    var backing: Kotlinx_coroutines_coreCompletableDeferred = ExposeAPIKt.finishedDeferred()
    var completableDeferred: Kotlinx_coroutines_coreCompletableDeferred {
        get {
            return deferToUse()
        }
    }
    
    var viewController: LoginViewController? = nil
    
    func deferToUse() -> Kotlinx_coroutines_coreCompletableDeferred {
        let newOrCurrent = ExposeAPIKt.activeDeferred(current: backing)
        backing = newOrCurrent
        return newOrCurrent
    }
    
    func back() {
        
    }

    func complete(data: Any?) {
        completableDeferred.complete(value: ExposeAPIKt.complete(output: data))
    }

    func error(error: KotlinThrowable) {
        
    }
}

// copied from login
class ErrorUIProxyImpl: ErrorUIProxy {
    var input: Any? = nil
    var backing: Kotlinx_coroutines_coreCompletableDeferred = ExposeAPIKt.finishedDeferred()
    var completableDeferred: Kotlinx_coroutines_coreCompletableDeferred {
        get {
            return deferToUse()
        }
    }
    
    func deferToUse() -> Kotlinx_coroutines_coreCompletableDeferred {
        let newOrCurrent = ExposeAPIKt.activeDeferred(current: backing)
        backing = newOrCurrent
        return newOrCurrent
    }
    
    func back() {
        
    }

    func complete(data: Any?) {
        
    }

    func error(error: KotlinThrowable) {
        
    }
}
