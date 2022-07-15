/*
  SPDX-License-Identifier: Apache-2.0
*/

import {Object, Property} from 'fabric-contract-api';

@Object()
export class User {
    @Property()
    public docType?: string;

    @Property()
    public Username: string;

    @Property()
    public Identity: string;

    @Property()
    public Password: string;

    @Property()
    public MspId: string;

    @Property()
    public LoggedIn: boolean;
}
