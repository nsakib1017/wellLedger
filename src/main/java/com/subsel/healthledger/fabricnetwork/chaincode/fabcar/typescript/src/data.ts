/*
  SPDX-License-Identifier: Apache-2.0
*/

import {Object, Property} from 'fabric-contract-api';

@Object()
export class Data {
    @Property()
    public docType?: string;

    @Property()
    public ID: string;

    @Property()
    public Key: string;

    @Property()
    public Username: string;

    @Property()
    public Type: string;

    @Property()
    public Data: string;

    @Property()
    public Issued: string;

    @Property()
    public Maturity: string;
}
