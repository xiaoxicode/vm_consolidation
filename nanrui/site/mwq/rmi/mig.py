
import sys
import libvirt

source = sys.argv[1]
dest = sys.argv[2]
vm = sys.argv[3]

conn_sour = libvirt.open('qemu+tcp://qwm@'+source+'/system')
conn_dest = libvirt.open('qemu+tcp://qwm@'+dest+'/system')
  
#search vm
vm_domain = conn_sour.lookupByName(vm)
   
#the meaning of parameters :(self._o, dconn__o, flags, dname, uri, bandwidth)
#connection of source PM, whether live migration, vm name, uri, bandwidth
print vm_domain.migrate(conn_dest,True,vm,None,0)
  
