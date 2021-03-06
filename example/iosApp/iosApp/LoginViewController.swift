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

class LoginViewController: FSMViewController<LoginUIProxy, KotlinUnit, LoginNavFSMCredentials> {
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
    
    static func newInstance() -> UIViewController {
        var vcType = String(describing: type(of: self))
        vcType.removeLast(5) // for ".type" suffix
        let storyboard: UIStoryboard = UIStoryboard(name: vcType, bundle: nil)
        return storyboard.instantiateViewController(identifier: vcType)
    }
}

// optional to use, can implement FSMViewControllerProtocol directly on ViewController
class FSMViewController<ProxyType: NFSMUIProxy, Input, Output> : UIViewController, FSMViewControllerP {
    typealias Proxy = ProxyType
    typealias In = Input
    typealias Out = Output

    var proxy: Proxy? = nil
}

protocol FSMViewControllerP {
    associatedtype Proxy: NFSMUIProxy
    associatedtype In
    associatedtype Out
    
    var proxy: Proxy? { get set }
}

extension FSMViewControllerP {
    func complete(output: Out) {
        proxy!.complete(data: output)
    }
}
