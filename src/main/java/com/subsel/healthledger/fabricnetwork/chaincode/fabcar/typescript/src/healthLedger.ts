/*
 * SPDX-License-Identifier: Apache-2.0
 */

import {Context, Contract, Info, Returns, Transaction} from 'fabric-contract-api';
import { Data } from './data';
import { User } from './user';

@Info({title: 'HealthLedger', description: 'Smart contract for health ledger'})
export class HealthLedgerContract extends Contract {

    @Transaction()
    public async InitLedger(ctx: Context): Promise<void> {
        const users: User[] = [
            {
                Username: 'genesis',
                Identity: 'blue',
                Password: 'saltedP',
                MspId: 'Org1Msp',
                LoggedIn: false
            }
        ];
        const data: Data[] = [
            {   
                ID: '0',
                Key: 'infinity',
                Username: 'genesis',
                Type: 'saltedP',
                Data: 'QmHash',
                Issued: 'begining',
                Maturity: 'end'
            }
        ];
        for (const user of users) {
            user.docType = 'user';
            await ctx.stub.putState(user.Username, Buffer.from(JSON.stringify(user)));
            console.info(`User ${user.Username} initialized`);
        }
        for (const datum of data) {
            datum.docType = 'data';
            await ctx.stub.putState(datum.ID, Buffer.from(JSON.stringify(datum)));
            console.info(`User ${datum.ID} initialized`);
        }
    }

    @Transaction()
    public async Register(ctx: Context, username: string, identity: string, password: string, mspId: string){
        const exists = await this.UserExists(ctx, username);
        if(exists){
            throw new Error(`The user ${username} already exists`);
        }

        const user: User = {
            docType: "user",
            Identity: identity,
            Username: username,
            Password: password,
            MspId: mspId,
            LoggedIn: false
        }
        await ctx.stub.putState(username, Buffer.from(JSON.stringify(user)));
    }

    @Transaction()
    public async LogIn(ctx: Context, username: string, password: string){
        const userString = await this.ReadUser(ctx, username);
        const user = JSON.parse(userString);
        if(user.length !== 0 && user.Password === password){
            user.LoggedIn = true;
            await ctx.stub.putState(username, Buffer.from(JSON.stringify(user)));
        } else {
            throw new Error(`The credentials does not match`)
        }
    }

    @Transaction()
    public async LogOut(ctx: Context, username: string){
        const userString = await this.ReadUser(ctx, username);
        const user = JSON.parse(userString);
        user.LoggedIn = false;
        await ctx.stub.putState(username, Buffer.from(JSON.stringify(user)));
    }

    // CreateAsset issues a new asset to the world state with given details.
    @Transaction()
    public async CreateEhr(ctx: Context, id: string, key: string, uname:string, type: string, data: string, issued: string, maturity: string): Promise<void> {
        const exists = await this.EhrExists(ctx, id);
        if (exists) {
            throw new Error(`The asset ${id} already exists`);
        }

        const ehrData: Data = {
            docType: "data",
            ID: id,
            Key: key,
            Type: type,
            Username: uname,
            Data: data,
            Issued: issued,
            Maturity: maturity
        };
        await ctx.stub.putState(id, Buffer.from(JSON.stringify(ehrData)));
    }

    // ReadAsset returns the asset stored in the world state with given id.
    @Transaction(false)
    public async ReadEhr(ctx: Context, id: string): Promise<string> {
        const ehrJSON = await ctx.stub.getState(id); // get the asset from chaincode state
        if (!ehrJSON || ehrJSON.length === 0) {
            throw new Error(`The ehr ${id} does not exist`);
        }
        return ehrJSON.toString();
    }

    // ReadAsset returns the asset stored in the world state with given id.
    @Transaction(false)
    public async ReadUser(ctx: Context, username: string): Promise<string> {
        const userJSON = await ctx.stub.getState(username); // get the asset from chaincode state
        if (!userJSON || userJSON.length === 0) {
            throw new Error(`The asset ${username} does not exist`);
        }
        return userJSON.toString();
    }

    // DeleteAsset deletes an given asset from the world state.
    @Transaction()
    public async DeleteTempEhr(ctx: Context, id: string): Promise<void> {
        const exists = await this.EhrExists(ctx, id);
        if (!exists) {
            throw new Error(`The asset ${id} does not exist`);
        }
        return ctx.stub.deleteState(id);
    }

    // AssetExists returns true when asset with given ID exists in world state.
    @Transaction(false)
    @Returns('boolean')
    public async EhrExists(ctx: Context, id: string): Promise<boolean> {
        const ehrJSON = await ctx.stub.getState(id);
        return ehrJSON && ehrJSON.length > 0;
    }

    @Transaction(false)
    @Returns('boolean')
    public async UserExists(ctx: Context, username: string): Promise<boolean> {
        const userJSON = await ctx.stub.getState(username);
        return userJSON && userJSON.length > 0;
    }

    @Transaction(false)
    @Returns('boolean')
    public async UserIsLoggedIn(ctx: Context, username: string): Promise<boolean> {
        const userString = await this.ReadUser(ctx, username);
        const user = JSON.parse(userString);
        return user.LoggedIn ? user.LoggedIn : false;
    }

    // TransferAsset updates the owner field of asset with given id in the world state.
    @Transaction()
    public async ChangeData(ctx: Context, id: string, data: string): Promise<void> {
        const datumString = await this.ReadEhr(ctx, id);
        const datum = JSON.parse(datumString);
        datum.Data = data;
        await ctx.stub.putState(id, Buffer.from(JSON.stringify(datum)));
    }

    // TransferAsset updates the owner field of asset with given id in the world state.
    @Transaction()
    public async ExtendLimit(ctx: Context, id: string, maturity: string): Promise<void> {
        const datumString = await this.ReadEhr(ctx, id);
        const datum = JSON.parse(datumString);
        datum.Maturity = maturity;
        await ctx.stub.putState(id, Buffer.from(JSON.stringify(datum)));
    }

    // GetAllAssets returns all assets found in the world state.
    @Transaction(false)
    @Returns('string')
    public async GetAllEhrByUser(ctx: Context, uname: string): Promise<string> {
        const allResults = [];
        // range query with empty string for startKey and endKey does an open-ended query of all assets in the chaincode namespace.
        // const query = `{"selector":{"docType":"user", "uname": "${uname}"}`
        const query = {
            selector: {
                docType: 'data',
                Username: uname
            }
        };
        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        let result = await iterator.next();
        while (!result.done) {
            const strValue = Buffer.from(result.value.value.toString()).toString('utf8');
            let record;
            try {
                record = JSON.parse(strValue);
            } catch (err) {
                console.log(err);
                record = strValue;
            }
            allResults.push({Key: result.value.key, Record: record});
            result = await iterator.next();
        }
        return JSON.stringify(allResults);
    }

    @Transaction(false)
    @Returns('string')
    public async GetAllEhr(ctx: Context): Promise<string> {
        const allResults = [];
        // range query with empty string for startKey and endKey does an open-ended query of all assets in the chaincode namespace.
        // const query = `{"selector":{"docType":"user", "uname": "${uname}"}`
        const query = {
            selector: {
                docType: 'data',
            }
        };
        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        let result = await iterator.next();
        while (!result.done) {
            const strValue = Buffer.from(result.value.value.toString()).toString('utf8');
            let record;
            try {
                record = JSON.parse(strValue);
            } catch (err) {
                console.log(err);
                record = strValue;
            }
            allResults.push({Key: result.value.key, Record: record});
            result = await iterator.next();
        }
        return JSON.stringify(allResults);
    }
    
    @Transaction(false)
    @Returns('string')
    public async GetAllUser(ctx: Context): Promise<string> {
        const allResults = [];
        // range query with empty string for startKey and endKey does an open-ended query of all assets in the chaincode namespace.
        // const query = `{"selector":{"docType":"user", "uname": "${uname}"}`
        const query = {
            selector: {
                docType: 'user'
            }
        };
        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        let result = await iterator.next();
        while (!result.done) {
            const strValue = Buffer.from(result.value.value.toString()).toString('utf8');
            let record;
            try {
                record = JSON.parse(strValue);
            } catch (err) {
                console.log(err);
                record = strValue;
            }
            allResults.push({Key: result.value.key, Record: record});
            result = await iterator.next();
        }
        return JSON.stringify(allResults);
    }
    
}