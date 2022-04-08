package elements;

import elements.gas.*;
import elements.liquid.*;
import elements.solid.immovable.*;
import elements.solid.movable.*;

import java.util.*;
import java.util.stream.Collectors;

public enum ElementType
{
    EMPTYCELL(EmptyCell.class, ClassType.EMPTYCELL)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new EmptyCell(x, y);
        }
    },
    PARTICLE(Particle.class, ClassType.PARTICLE)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            throw new IllegalStateException();
        }
    },
    SAND(Sand.class, ClassType.MOVABLESOLID)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new Sand(x, y);
        }
    },
    COAL(Coal.class, ClassType.MOVABLESOLID)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new Coal(x, y);
        }
    },
    IRON(Iron.class, ClassType.IMMOVABLESOLID)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new Iron(x, y);
        }
    },
    WALL(Iron.class, ClassType.IMMOVABLESOLID)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new Wall(x, y);
        }
    },
    WATER(Water.class, ClassType.LIQUID)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new Water(x, y);
        }
    },
    ACID(Water.class, ClassType.LIQUID)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new Acid(x, y);
        }
    },
    HF(HF.class, ClassType.GAS)
    {
        @Override
        public Element createElementByMatrix(int x, int y)
        {
            return new HF(x, y);
        }
    };

    public final Class<? extends Element> clazz;
    public final ClassType classType;
    public static List<ElementType> IMMOVABLE_SOLIDS;
    public static List<ElementType> MOVABLE_SOLIDS;
    public static List<ElementType> SOLIDS;
    public static List<ElementType> LIQUIDS;
    public static List<ElementType> GASES;


    ElementType(Class<? extends Element> clazz, ClassType classType)
    {
        this.clazz = clazz;
        this.classType = classType;
    }

    public abstract Element createElementByMatrix(int x, int y);

    public static List<ElementType> getMovableSolids()
    {
        if (MOVABLE_SOLIDS == null)
        {
            MOVABLE_SOLIDS = initializeList(ClassType.MOVABLESOLID);
            MOVABLE_SOLIDS.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(MOVABLE_SOLIDS);
    }

    public static List<ElementType> getImmovableSolids()
    {
        if (IMMOVABLE_SOLIDS == null)
        {
            IMMOVABLE_SOLIDS = initializeList(ClassType.IMMOVABLESOLID);
            IMMOVABLE_SOLIDS.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(IMMOVABLE_SOLIDS);
    }

    public static List<ElementType> getSolids()
    {
        if (SOLIDS == null)
        {
            List<ElementType> immovables = new ArrayList<>(getImmovableSolids());
            immovables.addAll(getMovableSolids());
            SOLIDS = immovables;
            immovables.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(SOLIDS);
    }

    public static List<ElementType> getLiquids()
    {
        if (LIQUIDS == null)
        {
            LIQUIDS = initializeList(ClassType.LIQUID);
            LIQUIDS.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(LIQUIDS);
    }

    public static List<ElementType> getGases()
    {
        if (GASES == null)
        {
            GASES = initializeList(ClassType.GAS);
            GASES.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(GASES);
    }

    private static List<ElementType> initializeList(ClassType classType)
    {
        return Arrays.stream(ElementType.values()).filter(elementType -> elementType.classType.equals(classType)).collect(Collectors.toList());
    }

    public enum ClassType
    {
        MOVABLESOLID,
        IMMOVABLESOLID,
        LIQUID,
        GAS,
        PARTICLE,
        EMPTYCELL,
        PLAYER;

    }
}
