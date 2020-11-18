//
//  TestViewController.swift
//  iosApp
//
//  Created by James Hunt on 11/17/20.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import UIKit

class TestViewController: UIViewController {
    
    static func storyboardInstance() -> TestViewController? {
        var vcType = String(describing: type(of: self))
        vcType.removeLast(5) // for ".type" suffix
        let storyboard: UIStoryboard = UIStoryboard(name: vcType, bundle: nil)
        return storyboard.instantiateViewController(identifier: vcType) as? TestViewController
    }
    
    override func viewDidLoad() {
        print("hello")
    }
}
