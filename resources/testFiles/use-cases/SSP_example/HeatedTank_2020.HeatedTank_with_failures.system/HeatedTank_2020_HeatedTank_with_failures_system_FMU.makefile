# FIXME: before you push into master...
RUNTIMEDIR=/Users/olero90/OpenModelica/build/include/omc/c/
#COPY_RUNTIMEFILES=$(FMI_ME_OBJS:%= && (OMCFILE=% && cp $(RUNTIMEDIR)/$$OMCFILE.c $$OMCFILE.c))

fmu:
	rm -f HeatedTank_2020_HeatedTank_with_failures_system.fmutmp/sources/HeatedTank_2020_HeatedTank_with_failures_system_init.xml
	cp -a "/Users/olero90/OpenModelica/build/share/omc/runtime/c/fmi/buildproject/"* HeatedTank_2020_HeatedTank_with_failures_system.fmutmp/sources
	cp -a HeatedTank_2020_HeatedTank_with_failures_system_FMU.libs HeatedTank_2020_HeatedTank_with_failures_system.fmutmp/sources/

