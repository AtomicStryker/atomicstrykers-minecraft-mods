
function initializeCoreMod() {
    print("Finder Compass cminit");

    function printObject(o) {
        var out = '';
        for (var p in o) {
            out += p + ': ' + o[p] + '\n';
        }
        print(out);
    }

    return {
        'Finder Compass Render Hook': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.ItemRenderer'
            },
            'transformer': function(classNode) {

                print("Visiting classnode ", classNode.name);

                var methods = classNode.methods;
                for(var m in methods) {
                    print("looking at method ", m);
                    printObject(m);
                }

                return classNode;
            }
        }
    }
}