import yawp from './yawp';

//export default yawp;

export default class Library {
    constructor() {
        this._name = 'Library';
    }

    get name() {
        return this._name;
    }
}
