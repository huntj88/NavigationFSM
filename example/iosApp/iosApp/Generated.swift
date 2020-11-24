//
//  Generated.swift
//  iosApp
//
//  Created by James Hunt on 11/21/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import UIKit
import shared

func newVC(proxy: NFSMUIProxy) -> UIViewController {
    switch String(describing: type(of: proxy)) {
    case "LoginUIProxyImpl":
        return LoginViewController.newVC(proxy: proxy) as LoginViewController
    default:
        // TODO: error proxy
        return UIViewController()
    }
}

extension LoginViewController {
    static func newVC(proxy: NFSMUIProxy) -> LoginViewController {
        let vc = LoginViewController.newInstance() as! LoginViewController
        let castProxy = proxy as! LoginUIProxyImpl
        vc.proxy = castProxy
        castProxy.viewController = vc
        return vc
    }
}

func createUIRegistry() -> [UIRegistryEntry] {
    return [
        UIRegistryEntry(kClass: ExposeAPIKt.loginUIProxyKClass(), newInstance: {
            return LoginUIProxyImpl()
        }),
        UIRegistryEntry(kClass: ExposeAPIKt.errorUIProxyKClass(), newInstance: {
            return ErrorUIProxyImpl()
        })
    ]
}

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
         completableDeferred.complete(value: ExposeAPIKt.complete(output: data))
    }

    func error(error: KotlinThrowable) {
        
    }
}
