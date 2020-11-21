//
//  TestViewController.swift
//  iosApp
//
//  Created by James Hunt on 11/17/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import UIKit
import shared

class LoginViewController: UIViewController, FSMViewControllerProtocol {
    typealias Proxy = LoginUIProxy
    typealias In = KotlinUnit
    typealias Out = LoginNavFSMCredentials
    
    var proxy: LoginUIProxyImpl? = nil
    
    @IBOutlet weak var label: UILabel!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        print("input into LoginViewController:")
        print(proxy?.input ?? "nil input")

        let tap = UITapGestureRecognizer(
            target: self,
            action: #selector(LoginViewController.tapFunction)
        )
        label.isUserInteractionEnabled = true
        label.addGestureRecognizer(tap)
    }
    
    @IBAction func tapFunction(sender: UITapGestureRecognizer) {
        complete(output: LoginNavFSMCredentials(username: "wow", password: "not wo"))
    }
    
    static func newVC(proxy: NFSMUIProxy) -> LoginViewController {
        let vc = LoginViewController.storyboardInstance()
        let castProxy = proxy as! LoginUIProxyImpl
        vc.proxy = castProxy
        castProxy.viewController = vc
        return vc
    }
    
    private static func storyboardInstance() -> LoginViewController {
        var vcType = String(describing: type(of: self))
        vcType.removeLast(5) // for ".type" suffix
        let storyboard: UIStoryboard = UIStoryboard(name: vcType, bundle: nil)
        return storyboard.instantiateViewController(identifier: vcType) as LoginViewController
    }
}

protocol FSMViewControllerProtocol {
    associatedtype Proxy: NFSMUIProxy
    associatedtype In
    associatedtype Out
    
    var proxy: LoginUIProxyImpl? { get set }
}

extension FSMViewControllerProtocol {
    func complete(output: Out) {
        proxy!.complete(data: output)
    }
}
