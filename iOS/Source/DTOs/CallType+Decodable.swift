// Copyright © 2018-2023 Kaleyra S.p.a. All Rights Reserved.
// See LICENSE for licensing information

import Foundation

struct UnknownCallTypeError: Error {}

extension CallType: Decodable {

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        try self.init(try container.decode(String.self))
    }

    init(_ string: String) throws {
        switch string {
            case "audio":
                self = .audio
            case "audioUpgradable":
                self = .audioUpgradable
            case "audioVideo":
                self = .audioVideo
            default:
                throw UnknownCallTypeError()
        }
    }
}
