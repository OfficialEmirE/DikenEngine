package me.ramazanenescik04.diken;

/**
 * Represents the `SystemInfo` type within the DikenEngine `core` package.
 */
public class SystemInfo {
	
	public enum OS {
	    WINDOWS("win"),
	    LINUX("linux", "nux", "nix"),
	    MACOS("mac", "darwin"),
	    ANDROID("android"),
	    IOS("ios"),
	    FREEBSD("freebsd"),
	    OPENBSD("openbsd"),
	    NETBSD("netbsd"),
	    DRAGONFLYBSD("dragonfly"),
	    SOLARIS("solaris"),
	    AIX("aix"),
	    HPUX("hp-ux"),
	    ILLUMOS("illumos"),
	    CHROME_OS("cros"),
	    FUCHSIA("fuchsia"),
	    QNX("qnx"),
	    HAIKU("haiku"),
	    MINIX("minix"),
	    PLAN9("plan9"),
	    REDOX("redox"),
	    RISCOS("riscos"),
	    OS2("os/2", "os2"),
	    BEOS("beos"),
	    AMIGAOS("amiga"),
	    AROS("aros"),
	    MORPHOS("morphos"),
	    MS_DOS("ms-dos", "dos"),
	    CP_M("cp/m"),
	    SYMBIAN("symbian"),
	    PALM_OS("palm"),
	    VMS("vms", "openvms"),
	    Z_OS("z/os", "zos"),
	    INFERNO("inferno"),
	    TEMPLE_OS("templeos"),
	    SKY_OS("skyos"),
	    MENUET_OS("menuet"),
	    KOLIBRIOS("kolibri"),
	    SERENITY_OS("serenity"),
	    REACT_OS("reactos"),
	    HURD("hurd"),
	    GNU("gnu"),
	    UNICOS("unicos"),
	    VXWORKS("vxworks"),
	    RTEMS("rtems"),
	    THREADX("threadx"),
	    CONTIKI("contiki"),
	    RIOT_OS("riot"),
	    NUTTX("nuttx"),
	    EMBEDDED_LINUX("embedded"),
	    MOUNTA("mounta"),
	    PENOS("penos"),
	    OTHER();

	    private final String[] matchers;

	    OS(String... matchers) {
	        this.matchers = matchers;
	    }

	    public static OS detect(String osName) {
	        osName = osName.toLowerCase();
	        for (OS os : values()) {
	            for (String matcher : os.matchers) {
	                if (osName.contains(matcher)) {
	                    return os;
	                }
	            }
	        }
	        return OTHER;
	    }
	}

	
	public enum Architecture {
		X86("x86", "i386", "i486", "i586", "i686"),
	    X86_64("x86_64", "amd64"),
	    ARM("arm"),
	    ARM64("aarch64", "arm64"),
	    MIPS("mips"),
	    MIPS64("mips64"),
	    POWERPC("ppc", "powerpc"),
	    POWERPC64("ppc64", "powerpc64"),
	    SPARC("sparc"),
	    SPARC64("sparcv9", "sparc64"),
	    RISCV32("riscv32"),
	    RISCV64("riscv64"),
	    ITANIUM("ia64", "itanium"),
	    ALPHA("alpha"),
	    S390("s390"),
	    S390X("s390x"),
	    AVR("avr"),
	    MSP430("msp430"),
	    XTENSA("xtensa"),
	    SUPERH("sh", "superh"),
	    VAX("vax"),
	    PDP11("pdp11"),
	    OPENRISC("or1k", "openrisc"),
	    UNKNOWN();

	    private final String[] matchers;

	    Architecture(String... matchers) {
	        this.matchers = matchers;
	    }
	    
	    public static Architecture detect(String archName) {
	        archName = archName.toLowerCase();
	        for (Architecture arch : values()) {
	            for (String matcher : arch.matchers) {
	                if (archName.contains(matcher)) {
	                    return arch;
	                }
	            }
	        }
	        return UNKNOWN;
	    }
	}
	
	public static final SystemInfo instance = new SystemInfo();
	
	private SystemInfo.OS _os;
	private SystemInfo.Architecture _arch;

	private SystemInfo() {
		String osName = System.getProperty("os.name");
        String archName = System.getProperty("os.arch");

        _os = OS.detect(osName);
        _arch = Architecture.detect(archName);
	}
	
	public final SystemInfo.OS getOS() {
		return _os;
	}
	
	public final SystemInfo.Architecture getArch() {
		return _arch;
	}
}
