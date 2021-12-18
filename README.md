## 线程基础知识

**JUC 四大口诀**

1. 高内聚低耦合前提下，封装思想。线程操作资源类。
2. 判断、干活、通知
3. 防止虚假唤醒，wait 方法要注意使用 while 判断
4. 注意标志位 flag，可能是 volatile 修饰的

<br>

 开始一个线程：Java 线程是通过 start() 方法启动执行的，主要内容在 native 的 start0() 方法中。Thread.java 对应的就是 Thread.c，start0() 其实就是 JVM_StartThread。

<br>

Java 多线程相关概念：

- 进程：是程序的一次执行，是系统进行资源分配和调度的独立单位，每一个进程都有它自己的内存空间和系统资源

- 线程：在同一个进程内，又可以执行多个任务，而每一个任务可以看做是一个线程；一个进程会有 1 和或多个线程的

- 管程：Monitor(监视器对象，管程对象)，就是我们平时说的锁。是一种同步器，它的义务是保证（同一时间）只有一个线程能够访问被保护的数据和代码。JVM 中同步是基于进入和退出监视器对象 (Monitor，管程对象)来实现的，每个对象实例都会有一个 Monitor 对象。Monitor 对象会和 Java 对象一同创建并销毁，底层是由 C++语言来实现的。

  > JVM第三版 6.4.10 同步指令
  >
  > Java 虚拟机 key 支持方法级的同步和方法内部一段指令序列的同步，这两种同步结构都是使用管程（Monitor，更常见的是直接将它称为“锁”）来实现的。
  >
  > 方法级的同步是隐式的，无需通过字节码指令来控制，它实现在方法调用和返回操作之中。虚拟机可以从方法常量池中的方法表结构中的 **ACC_SYNCHRONIZED** 访问标志得知一个方法是否被声明为同步方法。**当方法调用时，调用指令会检测方法的 ACC_SYNCHRONIZED 访问标志是否被设置，如果设置了，执行线程就要求先成功持有管程，然后才能执行方法，最后方法完成（无论是否正常完成）时释放管程。在方法执行期间，执行线程持有了管程，其它任何线程都无法再获取到同一个管程。**如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，那这个同步方法所持有的管程将在异常抛到同步方法边界之外时自动释放。
  >
  > 同步一段指令集序列通常是由 Java 语言中的 synchronized 语句块来表示的， Java 虚拟机的指令集中有 monitorenter 和 monitorexit 两条指令来支持 synchronized 关键字的语义，正确实现 synchronized 关键字需要 Javac 编译器与 Java 虚拟机两者共同协作支持。

<br>

用户线程和守护线程：Java 线程分为用户线程和守护线程，线程的 deamon 属性为 true，表示

- 守护线程：线程的 deamon 属性值为 true，表示是守护线程。是一种特殊的线程，在后台默默地完成一些系统性的服务，比如垃圾回收线程就是守护线程。
- 用户线程：线程的 deamon 属性值为 false，表示是用户线程。是系统工作线程，会完成这个程序需要完成的业务操作。**设置守护线程，需要在 start() 方法之前进行**。

> 当程序中所有用户线程执行完毕之后，不管守护线程是否结束，系统都会自动退出。如果用户线程全部结束了，意味着程序需要完成的业务操作已经结束了，系统可以退出了。所以，当系统只剩下守护线程的时候，Java 虚拟机就会自动退出。



---

## CompletableFuture

#### **Java 8 函数式编程的几个重要接口**

```java
// Runnable：无参数，无返回值
@FunctionalInterface
public interface Runnable {
    public abstract void run();
}

// Function：接收一个参数，有返回值
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}

// Consumer：接收一个参数，没有返回值
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}

// Supplier：无参数，有返回值
@FunctionalInterface
public interface Supplier<T> {
    T get();
}

// BiConsumer：接收两个参数，没有返回值
@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T t, U u);
}
```

| 函数式接口名称 | 方法名称         | 参数     | 返回值   |
| -------------- | ---------------- | -------- | -------- |
| Runnable       | run()            | 无参数   | 无返回值 |
| Function       | apply(T t)       | 一个参数 | 有返回值 |
| Consumer       | accept(T t)      | 一个参数 | 无返回值 |
| Supplier       | get()            | 无参数   | 无返回值 |
| BiConsumer     | accept(T t, U u) | 两个参数 | 无返回值 |

<br>

#### Future 接口 和 Callable 接口

- Future 接口定义了操作异步任务执行的一些方法，如获取异步任务的执行结果、取消任务的执行、判断任务是否被取消、判断任务执行是否完成等。

- Callable 接口中定义了需要有返回的任务需要实现的方法。比如主线程让子线程去执行任务，子线程可能比较耗时，启动子线程开始执行任务后，主线程可以去做其它的操作，过一段时间，再去获取子线程任务的执行结果。

  ```java
  @FunctionalInterface
  public interface Callable<V> {
      V call() throws Exception;
  }
  ```

<br>

#### FutureTask

<img src="img/FutureTask.jpg" alt="FutureTask" style="zoom:33%;" />

> A Future represents the result of an asynchronous computation. Methods are provided to check if the computation is complete, to wait for its completion, and to retrieve the result of the computation. The result can only be retrieved using method get when the computation has completed, blocking if necessary until it is ready. Cancellation is performed by the cancel method. Additional methods are provided to determine if the task completed normally or was cancelled. Once a computation has completed, the computation cannot be cancelled. If you would like to use a Future for the sake of cancellability but not provide a usable result, you can declare types of the form Future<?> and return null as a result of the underlying task.
>
> Future 表示异步计算的结果。提供了检查计算是否完成、等待计算完成以及检索计算结果的方法。结果只能在计算完成时使用方法 get 检索，必要时阻塞直到准备就绪。取消是通过 cancel 方法执行的。还提供了其他方法来确定任务是否正常完成或取消。一旦计算完成，就不能取消计算。如果您希望为了可取消性而使用 Future，但是没有提供可用的结果，那么可以声明 Future < ? > 表单的类型然后返回 null 作为底层任务的结果。
>
> FutureTask 的 get() 方法，是一个阻塞方法。一旦调用 get() 方法，不管是否计算完成，都会导致阻塞。
>
> FutureTask 的 isDone() 方法，采用的是轮询的方式查看计算结果，会消耗无畏的 CUP 资源，还不见得能够及时得到结算结果。如果想要异步获取结果，通常会以轮询的方式去获取结果，尽量不要使用阻塞的方式。

<br>

#### CompletableFuture

<img src="img/CompletableFuture.jpg" alt="CompletableFuture" style="zoom: 33%;" />

```java
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {}
```

CompletionStage：

- 代表异步计算过程中的某一个阶段，一个阶段完成以后可能会触发另一个阶段，有些类似 Linux 系统的管道分隔符传参数
- 一个阶段的计算执行可以是一个 Function、Consumer 或者 Runnable。
- 一个阶段的执行可能是被单个阶段的完成触发，也可能是由多个阶段一起触发

CompletableFuture：

- 在 Java 8 中，CompletableFuture 提供了非常强大的 Future 的扩展功能，可以帮助我们简化异步编程的复杂性，并且提供了函数式编程的能力，可以通过回调的方式处理计算结果，也提供了转换和组合 CompletableFuture 的方法
- 它可能代表一个明确完成的 Future，也可能代表一个完成阶段（CompletionStage），它支持在计算完成后触发一些函数或执行某些动作
- 它实现了 Future 接口和 CompletionStage 接口

<br>

#### CompletableFuture 的重要核心方法

- **runAsync：无返回值**

  ```java
  // 传参不含线程池，直接使用默认的 ForkJoinPool.commonPool() 作为它的线程池，执行异步代码
  public static CompletableFuture<Void> runAsync(Runnable runnable);
  // 传参含线程池，使用传入的线程池至此那个异步代码
  public static CompletableFuture<Void> runAsync(Runnable runnable,Executor executor);
  
  private static void runAsyncTest() throws InterruptedException, ExecutionException {
    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
      System.out.println(Thread.currentThread().getName()+"\t"+"-----come in");
      //暂停几秒钟线程
      try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
      System.out.println("-----task is over");
    });
    System.out.println(future.get());
  }
  
  /**
   * ForkJoinPool.commonPool-worker-9	-----come in
   * -----task is over
   * null
   */
  ```

- **supplyAsync：有返回值**

  ```java
  // 传参不含线程池，直接使用默认的 ForkJoinPool.commonPool() 作为它的线程池，执行异步代码
  public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);
  // 传参含线程池，使用传入的线程池至此那个异步代码
  public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,Executor executor);
  
  private static void supplyAsyncTest() throws InterruptedException, ExecutionException {
      CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
          System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
          //暂停几秒钟线程
          try {
              TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          return ThreadLocalRandom.current().nextInt(100);
      });
  
      System.out.println(completableFuture.get());
  }
  
  /**
   * ForkJoinPool.commonPool-worker-9	-----come in
   * 47
   */
  ```

- **异步任务结束后或发生异常后，回调某个对象的方法**

  ```java
  private static void callBackTest() {
      CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
          System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
          int result = ThreadLocalRandom.current().nextInt(10);
          //暂停几秒钟线程
          try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
          System.out.println("-----计算结束耗时1秒钟，result： "+result);
          if(result > 6)
          {
              // 模拟异常
              int age = 10/0;
          }
          return result;
      }).whenComplete((v,e) ->{ // 异步任务 supplyAsync 正常结束后，回调 whenComplete 方法
          if(e == null)
          {
              System.out.println("-----result: "+v);
          }
      }).exceptionally(e -> { // // 异步任务 supplyAsync 发生异常后，回调 exceptionally 方法
          System.out.println("-----exception: "+e.getCause()+"\t"+e.getMessage());
          return -44;
      });
  
      //主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:暂停3秒钟线程
      try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
  }
  
  /**
   * 方法正常运行结果：
   * ForkJoinPool.commonPool-worker-9	-----come in
   * -----计算结束耗时1秒钟，result： 5
   * -----result: 5
   */
  
  /**
   * 方法发生异常后的结果
   * ForkJoinPool.commonPool-worker-9	-----come in
   * -----计算结束耗时1秒钟，result： 9
   * -----exception: java.lang.ArithmeticException: / by zero	java.lang.ArithmeticException: / by zero
   */
  ```

<br>

#### CompletableFuture 的优点

1. 异步任务结束时，会自动回调某个对象的方法
2. 异步任务出错时，会自动回调某个对象的方法
3. 主线程设置好回调后，不再关心异步任务的执行，异步任务之间可以顺序执行

<br>

#### CompletableFuture 的常用方法

**示例代码 → CompletableFutureDemo**

1. 获取计算结果和触发计算

   ```java
   // 阻塞式获取结果，直到获取到结果后才结束方法
   public T get();
   
   // 设置超时时间的阻塞式获取结果，若获取结果超时了，就结束方法
   public T get(long timeout, TimeUnit unit);
   
   // 立即获取结果，不阻塞，若方法没有计算完成，则返回设定的替代结果，若计算完成，直接返回计算结果
   public T getNow(T valueIfAbsent);
   
   // 阻塞式获取结果，但发生异常时，会抛出未经检查的异常，而 get() 方法会抛出经检查的异常，可以被捕获，自定义处理或直接抛出。
   public T join();
   
   /**
    * 主动触发计算。
    * 当 completableFuture.get() 时发生阻塞，complete 可以结束阻塞，立即返回 complete 面里面的值
    * 当 completableFuture.get() 时立即获取到结果，complete 不会触发计算
    */
   public boolean complete(T value);
   ```

2. 对计算结果进行处理

   ```java
   // 计算结果存在依赖关系，这两个线程串行化（当前步骤发生异常，不走下一步，哪步出错，就停在哪步）
   public <U> CompletableFuture<U> thenApply;
   
   // 计算结果存在依赖关系，这两个线程串行化（当前步骤发生异常，携带异常参数继续走下一步）
   public <U> CompletableFuture<U> handle;
   ```

   > whenComplete 和 whenCompleteAsync 的区别：
   >
   > - whenComplete：是执行当前任务的线程继续执行 whenComplete 的任务
   > - whenCompleteAsync：是将 whenCompleteAsync 这个任务继续提交给线程池来执行

3. 对计算结果进行消费

   ```java
   // 任务之间顺序执行
   // 任务 A 执行完毕后执行 B，并且 B 不需要 A 的结果
   public CompletableFuture<Void> thenRun(Runnable action);
   
   // 任务 A 执行完毕后执行 B，B 需要 A 的结果，但是任务 B 无返回值
   public CompletableFuture<Void> thenAccept(Consumer<? super T> action);
   
   // 任务 A 执行完毕后执行 B，B 需要 A 的结果，同时任务 B 有返回值
   public <U> CompletableFuture<U> thenApply;
   ```

4. 对计算速度进行选用

   ```java
   // 选取运行快的任务进行计算
   public <U> CompletableFuture<U> applyToEither;
   ```

5. 对计算结果进行合并

   ```java
   // 两个 CompletionStage 任务都完成后，最终能把两个任务的结果一起交给 thenCombine 来处理。先完成的先等着，等待其它分支任务
   public <U,V> CompletableFuture<V> thenCombine;
   ```

---

## Java 锁

#### 8 锁案例



<br>

#### 乐观锁与悲观锁

- 乐观锁：认为自己在使用数据的时候不会有其它的线程来修改数据，所以不会加锁，只是在更新数据的时候取判断之前有没有别的线程更新了这个数据。如果这个数据没有被更新，当前线程将自己修改的数据成功写入；如果数据已经被其它线程更新了，则根据不同的实现方式，执行不同的操作。**乐观锁在 Java 中是通过无锁编程来实现的，最常采用的方式是 CAS 算法，Java 原子类中的递增操作就是通过 CAS 自旋实现的。**
- 悲观锁：认为自己在使用数据的时候一定有其它的线程来修改数据，因此在获取数据的时候会先加锁，确保数据不会被其它线程修改。synchronize 关键字和 Lock 的实现类都属于悲观锁。适合写操作比较多的场景，先加锁可以保证写操作时数据正确。**显式加锁之后再操作同步资源。**

<br>

#### 公平锁和非公平锁

FIFO（First In First Out） 属于公平锁，否则属于非公平锁。

按序排队属于公平锁，就是判断同步队列是否还有先驱节点的存在，如果没有，才能获取到锁，如果有，等待先驱节点释放锁。

先占先得属于非公平锁。

默认采用非公平锁的原因：

1. 恢复挂起的线程到真正锁的获取是有时间差的，从开发人员的角度来看这个时间微乎其微，但从 CPU 的角度看，这个时间差是很明显的，所**以非公平锁能够更充分利用 CPU 的时间片，尽量减少 CPU 空间状态时间；**
2. 使用多线程很重要的考量点是线程切换的开销，当采用非公平锁时，当1个线程请求锁获取同步状态，然后释放同步状态，因为不需要考虑是否还有前驱节点，所以刚释放锁的线程在此刻再次获取同步状态的概率就变得非常大，所以就减少了线程的开销。
3. 如果使用的是公平锁，虽然保证了排队的公平性，但可能导致长时间在排队等待获取锁，就造成了“锁饥饿”。
4. 如果为了提高吞吐量，使用非公平锁很合适，因为减少了很多线程的切换时间

<br>

#### 可重入锁

可重入锁又称递归锁。是指同一个线程在外层方法获取到锁的时候，在进入该线程的内层方法时，就会自动获取到锁（前提是锁对象是同一个对象），不会因为之前已经获取到的锁没有释放而阻塞。Java 中 ReentrantLock 和 synchronized 都是可重入锁，可重入锁的一个优点就是可一定程度上避免死锁。

可重入锁的种类：

1. 隐式锁；synchronized 关键字修饰的锁。

   > synchronized 可重入锁的实现原理：
   >
   > 1. 每个锁对象拥有一个锁计数器和一个指向持有该锁的线程的指针。
   > 2. 当执行 monitorenter 时，如果目标锁对象的计数器为零，那么说明它没有被其它线程所持有，Java 虚拟机会将该锁对象的持有线程设置为当前线程，并且将计数器加一
   > 3. 当执行 monitorenter 时，如果目标锁对象的计数器不为零，且锁对象持有的线程是当前线程，那么 Java 虚拟机可以将其计数器加一，否则需要等待，直至持有线程释放该锁。
   > 4. 当执行 monitorexit 时，Java 虚拟机需将锁对象的计数器减一，当计数器为零时，代表锁已经被释放了。

2. 显式锁：Lock 的实现类，例如 ReentrantLock。

   > lock.lock()：加锁
   >
   > lock.unlock()：解锁
   >
   > 显式锁的加锁与解锁需成对出现，即加几次锁就要解几次锁。

<br>

#### 死锁

死锁是指两个或以上的线程在执行过程中，因争夺资源而造成的一种**互相等待的现象**，若无外力干涉，那它们将无法推进下去，如果系统资源充足，进程的资源请求能够得到满足，死锁出现的可能性就低，否则就会因争夺有限的资源而陷入死锁。

```java
public static void main(String[] args) {

    final Object objectLockA = new Object();
    final Object objectLockB = new Object();

    new Thread(() -> {
        synchronized (objectLockA)
        {
            System.out.println(Thread.currentThread().getName()+"\t"+"自己持有A，希望获得B");
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            synchronized (objectLockB)
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"A-------已经获得B");
            }
        }
    },"A").start();

    new Thread(() -> {
        synchronized (objectLockB)
        {
            System.out.println(Thread.currentThread().getName()+"\t"+"自己持有B，希望获得A");
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            synchronized (objectLockA)
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"B-------已经获得A");
            }
        }
    },"B").start();

}
```

死锁的排查：

- 命令方式：

  > jps -l：查找所有 Java 进程
  >
  > jstack 进程编号：查看该进程详情

- 图形化工具方式：jconsole

<br>

#### 读锁（共享锁）与写锁（独占锁）

<br>

自旋锁

<br>

#### 无锁→独占锁→读写锁→邮戳锁

<br>

#### 无锁→偏向锁→轻量锁→重量锁



---

## LockSupport 与线程中断

#### 线程中断机制

线程中断：

- 一个线程不应该由其它线程来强制中断或停止，而是应该由线程自己自行停止，所以 Thread.stop()、Thread.suspend()、Thread.resume() 这些方法都已经被废弃了。
- 在 Java 中没有办法立即停止一条线程，然而停止线程却显得尤为重要，例如取消一个耗时操作，因此，Java 提供了一种用于停止线程的机制，中断。
- 中断只是一种协助机制，Java 没有给中断增加任何语法，中断的过程完全有程序员自己实现。
- 每个线程对象中有一个标识，用于标识线程是否被中断。该标识位为 true，表示中断；该标识位为 false，表示未中断。通过调用线程对象的 interrupt 方法将可以将该线程的标志位设置为 true；可以在其它线程中调用，也可以在自己的线程中调用.
- **若要中断一个线程，需要手动调用该线程的 interrupt 方法，该方法仅仅是将线程对象的中断标识设置成 true**，然后需要自行代码实现不断地检测当前线程的标识位，如果为 true，表示其它线程要求这条线程中断，后续逻辑需自行实现。

| 方法                                | 说明                                                         |
| ----------------------------------- | ------------------------------------------------------------ |
| public void interrupt()             | 实例方法，仅仅是设置线程的中断状态（标识位）为 true，不会立即停止线程 |
| public static boolean interrupted() | 静态方法，判断线程是否被中断，并清除当前中断状态。（底层传参 ClearInterrupted=true） |
| public boolean isInterrupted()      | 实例方法，（检测线程中断标识位）判断当前线程是否被中断。（底层传参 ClearInterrupted=false） |

<br>

使用中断标识停止线程：在需要中断的线程中不断监听中断状态标识位，一旦发生中断，就执行相应的中断处理业务逻辑。

1. 通过 volatile 变量实现

   ```java
   private static volatile boolean isStop = false;
   
   public static void main(String[] args) {
   
       new Thread(() -> {
           while(true)
           {
               if(isStop)
               {
                   System.out.println(Thread.currentThread().getName()+"线程------isStop = true,自己退出了");
                   break;
               }
               System.out.println("-------hello interrupt");
           }
       },"t1").start();
   
       //暂停几秒钟线程
       try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
       isStop = true;
   }
   ```

2. 通过 AtomicBoolean实现

   ```java
   private final static AtomicBoolean atomicBoolean = new AtomicBoolean(true);
   
   public static void main(String[] args) {
   
       Thread t1 = new Thread(() -> {
           while(atomicBoolean.get())
           {
               try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
               System.out.println("-----hello");
           }
       }, "t1");
       t1.start();
   
       try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
   
       atomicBoolean.set(false);
   }
   ```

3. 通过 Thread 的中断 API 方法实现

   ```java
   public static void main(String[] args) {
   
       Thread t1 = new Thread(() -> {
           while(true)
           {
               if(Thread.currentThread().isInterrupted())
               {
                   System.out.println("-----t1 线程被中断了，break，程序结束");
                   break;
               }
               System.out.println("-----hello");
           }
       }, "t1");
       t1.start();
   
       System.out.println("**************"+t1.isInterrupted());
       //暂停5毫秒
       try { TimeUnit.MILLISECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
     
     	// interrupt()：无返回值
       t1.interrupt();
     	// isInterrupted()：有返回值
       System.out.println("**************"+t1.isInterrupted());
   
   }
   ```

<br>

**interrupt() 方法说明：**

- 当一个线程调用 interrupt() 方法时，如果线程处于正常活动状态，那么会将该线程的中断标识位设置为 true，仅此而已。被设置中断标识的线程将继续正常运行，不收影响。所以 interrupt() 方法并不能真正的中断线程，需要被调用的线程自己进行配合才行；

- 如果线程处于被阻塞状态（例如处于 sleep、wait、join 等状态），此时在其它线程中调用当前线程对象的 interrupt() 方法，那么线程将立即退出被阻塞状态，并抛出一个 InterruptedException 异常。sleep() 方法抛出 InterruptedException 后，中断标识也被清空，置为 false。若在 catch 中没有再次使用 interrupt() 方法，将中断标识位再次设置为 true 的话，将导致程序无法停止，导致无限循环下去了

  ```java
  public static void main(String[] args) {
  
      Thread t1 = new Thread(() -> {
          while (true){
              if (Thread.currentThread().isInterrupted()){
                  System.out.println(Thread.currentThread().getName() + "线程----→ isInterrupted()=true，自己退出了");
                  break;
              }
              try {
                  TimeUnit.MILLISECONDS.sleep(20);	
              }catch (Exception e){
                  // 若没有 Thread.currentThread().interrupt()，程序将不会停止，因为 sleep() 抛出异常后，将中断标志位设置为 false
                  // Thread.currentThread().interrupt();
                  e.printStackTrace();
              }
              System.out.println("===测试===");
          }
      },"t1");
      t1.start();
  
      try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
      t1.interrupt();
  }
  ```

- 静态 interrupt() 方法。Thread.interrupt()：作用是测试当前线程是否被中断（检查中断标识位），返回一个 boolean，并清除中断状态。第二次再调用时，中断状态已经被清除，将返回一个 false。

  ```java
  public static void main(String[] args) {
  
      System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
      System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
      System.out.println("111111");
      Thread.currentThread().interrupt();
      System.out.println("222222");
      System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
      System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
  }
  ```

  > isInterrupted() 方法和静态 interrupt() 方法的对比都是判断线程是否被中断，但是 isInterrupted() 方法不会清除中断标识，而 interrupt() 方法会清空中断标识。
  >
  > 因为这两个方法都是调用同一个方法 private native boolean isInterrupted(boolean ClearInterrupted)，只不过传入的参数的值不一样
  >
  > isInterrupted() ：传入的参数是 ClearInterrupted=false，即不清楚中断标识位
  >
  > static interrupt()：传入的参数是 ClearInterrupted=true，即清楚中断标识位

<br>

#### LockSupport

LockSupport 是用来创建锁和其它同步类的基本线程阻塞原语。LockSupport 中的 park()、unpark() 方法的作用分别是阻塞线程和唤醒阻塞的线程。

LockSupport 类使用了一种名为 Permit（许可）的概念来做到**阻塞和唤醒线程**的功能。每个线程都有一个 Permit（许可），permit 只有两个值，1 和 0，默认时 0。可以把许可看成是一种（0，1）信号量（Semaphore），但与 Semaphore 不同的是，许可的累加上限是 1。

<br>

**线程等待和唤醒的方式：**

1. wait 和 notify：使用 Object 的 wait() 方法让线程等待；使用 Object 的 notify() 方法唤醒线程；

   - **Object 类中的 wait、notify、notifyAll 必须在 synchronized 内部使用（同步代码块内或同步方法内），否则会抛出异常 IllegalMonitorStateException。**

   - **Object 类中的 wait() 方法必须在 notify() 方法前运行，否则会造成等待中的线程无法被唤醒，程序无法结束。**

2. await 和 signal：使用 JUC 包中 Condition 的 await() 方法让线程等待；使用 signal() 方法唤醒线程。

   - **Condition 类的 await()、signal() 方法必须在 lock() 和 unlock() 内使用，否则会抛出异常 IllegalMonitorStateException。即有锁才能调用。**
   - **Condition 类的 await() 方法必须在 signal() 方法前运行，否则会造成阻塞的线程无法被唤醒，程序无法结束。即要先等待再唤醒**

3. park 和 unpar：使用 LockSupport 的 park() 方法阻塞线程，使用 unpark() 方法唤醒被阻塞的线程

   - **park() /park(Object blocker)  ：阻塞当前线程/阻塞传入的具体线程。permit 默认值是 0，所以一开始调用 park() 方法时，当前线程就会阻塞，直到其它线程将当前线程的 permit 的值设置为 1 时，park 方法就会被唤醒，然后会将 permit 的值再次设置为 0 并返回；**
   - **unpark(Thread thread)：唤醒处于阻塞状态的指定线程。调用后，就会将指定的 thread 线程的许可 permit 的值设置成 1（多次调用 unpark 方法，permit 值不会累加，permit 值还是 1），会自动唤醒该类，即之前被阻塞的 LockSupport.park() 方法会立即返回**
   - **park() 和 unpark() 方法的使用无锁块要求**
   - **park() 和 unpark() 方法的使用无顺序要求**
   - **park() 和 unpark() 方法的使用必须成对使用**

---

## Java 内存模型之 JMM

计算机存储结构，从本地磁盘到主内存，再到 CPU 缓存，也就是从硬盘到内存，再到 CPU。一般对应的程序的操作就是从数据库查数据到内存然后再到 CPU 进行计算。

因为有多级缓存（CPU 和物理主内存的速度不一致），CPU 的运行并不是直接操作主内存，而是先把主内存中的数据读取到高速缓存中，而内存的读和写操作的时候就会造成不一致的问题。

Java 虚拟机规范中试图定义一种 Java 内存模型（Java Memory Model，简称 JMM）来屏蔽掉各种硬件和操作系统的内存访问差异，以实现让 Java 程序在各种平台下都能达到一致的内存访问效果。

JMM（Java 内存模型 Java Memory Model，简称 JMM），本身是一种抽象的概念，并不真实存在。它仅仅描述的是一组约定或规范，通过这组规范定义了程序中（尤其是多线程）各个变量的读写访问方式，并决定一个线程对共享变量的写入何时以及如何变成对另一个线程可见，**关键技术点都是围绕多线程的原子性、可见性和有序性展开的**。

- 通过 JMM 来实现线程和主内存之间的抽象关系
- 屏蔽各个硬件平台和操作系统的内存访问差异，以实现 Java 程序在各种平台下都能到达到一直的内存访问效果。

<br>

JMM 三大特性

1. 可见性：指当一个线程修改了某一个共享变量的值，其它线程是否能否立即直到该变更。

   <img src="img/线程与主内存和工作内存之间的交互关系.jpg" style="zoom:33%;" />

   > Java中普通的共享变量不保证可见性，因为数据修改被写入内存的时机是不确定的，多线程并发下很可能出现"脏读"，所以每个线程都有自己的***工作内存\***，线程自己的工作内存中保存了该线程使用到的变量的主内存副本拷贝，线程对变量的所有操作（读取，赋值等 ）都必需在线程自己的工作内存中进行，而不能够直接读写主内存中的变量。不同线程之间也无法直接访问对方工作内存中的变量，线程间变量值的传递均需要通过主内存来完成。

2. 原子性：指一个操作是不可中断的，即多线程环境下，操作不能被其它线程干扰

3. 有序性：对于一个线程的执行代码而言，我们总是习惯性认为代码的执行总是从上到下，有序执行。但为了性能，编译器和处理器通常会对指令序列进行重新排序。指令重排可以保证串行语义一致，但没有义务保证多线程间的语义也一致，即可能产生"脏读"，简单说，两行以上不相干的代码在执行的时候有可能先执行的不是第一条，不见得是从上到下顺序执行，执行顺序会被优化。

   <img src="img/指令重排.jpg" style="zoom:50%;" />

   - 单线程环境里面确保程序最终执行结果和代码顺序执行的结果一致。处理器在进行重排序时必须要考虑指令之间的**数据依赖性**。

   - 多线程环境中线程交替执行，由于编译器优化重排的存在，两个线程中使用的变量能否保证一致性是无法确定的,结果无法预测。

<br>

JMM 规范下，多线程对变量的读写过程：由于JVM运行程序的实体是线程，而每个线程创建时，JVM 都会为其创建一个工作内存(有些地方称为栈空间)，工作内存是每个线程的私有数据区域，而 Java 内存模型中规定所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问。但线程对变量的操作(读取赋值等)必须在工作内存中进行，首先要将变量从主内存拷贝到的线程自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，各个线程中的工作内存中存储着主内存中的**变量副本拷贝**，因此不同的线程间无法访问对方的工作内存，线程间的通信(传值)必须通过主内存来完成，其简要访问过程如下图:

<img src="img/多线程对变量的读写过程.jpeg" style="zoom: 33%;" />

> 我们定义的所有共享变量都储存在物理主内存中
>
> 每个线程都有自己独立的工作内存，里面保存该线程使用到的变量的副本(主内存中该变量的一份拷贝)
>
> 线程对共享变量所有的操作都必须先在线程自己的工作内存中进行后写回主内存，不能直接从主内存中读写(不能越级)
>
> 不同线程之间也无法直接访问其他线程的工作内存中的变量，线程间变量值的传递需要通过主内存来进行(同级不能相互访问)

<br>

JMM 规范下，多线程先行发生原则（happens-before）

- 如果一个操作 happens-before 另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。
- 两个操作之间存在happens-before关系，并不意味着一定要按照happens-before原则制定的顺序来执行。如果重排序之后的执行结果与按照happens-before关系来执行的结果一致，那么这种重排序并不非法。

<br>

happens-before 的 8 条规则

1. 次序规则：一个线程内，按照代码顺序，写在前面的操作先行发生于写在后面的操作。前一个操作的结果可以被后续的操作获取。例如前面一个操作把变量 X 赋值为 1，那后面一个操作肯定能知道 X 已经变成了 1。
2. 锁定规则：一个 unLock 操作先行发生于后面((这里的“后面”是指时间上的先后))对同一个锁的 lock 操作；
3. volatile 变量规则：对一个 volatile 变量的写操作先行发生于后面对这个变量的读操作，前面的写对后面的读是可见的，这里的“后面”同样是指时间上的先后。
4. 传递规则：如果操作 A 先行发生于操作 B，而操作 B 又先行发生于操作 C，则可以得出操作 A 先行发生于操作 C；
5. 线程启动规则（Thread Start Rule）：Thread 对象的 start() 方法先行发生于此线程的每一个动作
6. 线程中断规则（Thread Interruption Rule）：对线程 interrupt() 方法的调用先行发生于被中断线程的代码检测到中断事件的发生；即先中断，然才能检测到线程的中断。
7. 线程终止规则（Thread Termination Rule）：线程中的所有操作都先行发生于对此线程的终止检测，我们可以通过 Thread::join() 方法是否结束、Thread::isAlive() 的返回值等手段检测线程是否已经终止执行。
8. 对象终结规则（Finalizer Rule）：一个对象的初始化完成（构造函数执行结束）先行发生于它的 finalize() 方法的开始。即对象没有完成初始化之前，是不能调用finalized()方法的。

---

## Java 内存模型之 volatile

volatile 的内存语义

- 当写一个 volatile 变量时，JMM 会把该线程对应的本地内存中的共享变量值立即刷新回主内存中。
- 当读一个 volatile 变量时，JMM 会把该线程对应的本地内存设置为无效，直接从主内存中读取共享变量
- 所以 volatile 的写内存语义是直接刷新到主内存中，读的内存语义是直接从主内存中读取。

<br>

内存屏障（Memory Barriers / Fences）：

- 也称之为内存栅栏，内存栅障， 屏障指令等，是一类同步屏障指令，使得 CPU 或编译器对屏障指令的**前**面和**后**面所发出的内存操作执行一个排序的约束。**内存屏障其实就是一种 JVM 指令，Java 内存模型的重排规则会要求 Java 编译器在生成 JVM 指令时，插入特定的内存屏障指令，通过这些内存屏障指令，volatile 实现了 Java 内存模型中的可见性和有序性，但 volatile 无法保证原子性。**
- 可以阻止屏障两边的指令重排
- 写数据时加入内存屏障，可以强制将线程私有的工作内存中的数据刷回主内存
- 读数据时加入内存平展，可以将线程私有工作内存中的数据失效，重新到主内存中读取最新的数据

>  volatile 会在 Class 的内部的 Field 的 flags 添加一个 ACC_VOLATILE。JVM 在将字节码生成及其码的时候，当检测到操作是使用 volatile 修饰的话，就会根据 JMM 要求，在相应的位置插入内存屏障指令。

<br>

JVM 中提供了四类内存屏障指令

| 屏障类型   | 指令示例                   | 说明                                                         |
| ---------- | -------------------------- | ------------------------------------------------------------ |
| LoadLoad   | Load1；LoadLoad；Load2     | 保证 Load1 的读操作在 Load2 之前执行                         |
| StoreStore | Store1；StoreStore；Store2 | 在 Store2 及其后的写操作执行前，保证 Store1 的写操作已经刷新到主内存 |
| LoadStore  | Load1；LoadStore；Store2   | 在 Store2 及其后的写操作执行前，保证 Load1 的读操作已经读取结束 |
| StoreLoad  | Store1；StoreLoad；Load2   | 保证 Store1的写操作已经刷新到主内存之后，Load2 及其后的读操作才能执行 |

- 写
  - 在每一个 volatile 写操作的前面插入一个StoreStore 屏障
  - 在每一个 volatile 写操作后面插入一个 StoreLoad 屏障
- 读
  - 在每一个 volatile 读操作的后面插入一个 LoadLoad 屏障
  - 在每一个 volatile 读操作的后面插入一个 LoadStore 屏障

<br>

happens-before 之 volatile 变量规则：

| 第一个操作  | 第二个操作：普通读写 | 第二个操作：volatile 读 | 第二个操作 volatile 写 |
| ----------- | -------------------- | ----------------------- | ---------------------- |
| 普通读写    | 可以重排             | 可以重排                | 不可以重排             |
| volatile 读 | 不可以重排           | 不可以重排              | 不可以重排             |
| volatile 写 | 可以重排             | 不可以重排              | 不可以重排             |

- 当第一个操作为 volatile 读时，不论第二个操作是什么，都不能重排序。这个操作保证了 volatile 读之后的操作不会被重排的 volatile 读之前。
- 当第二个操作为 volatile 写时，不论第一个操作是什么，都不能重排序。这个操作保证了 volatile 写之前的操作不会被重排的 volatile 写之后。
- 当第一个操作为 volatile 写时，第二个操作为 volatile 时，不能重排。

<br>

volatile 的可见性：保证不同线程对这个变量进行操作时的可见性，即变量一旦改变，所有线程立即可见。

- 对一个 volatile 修饰的变量进行读操作的话，总能够读取到这个变量的最新的值，也就是这个变量最后被修改的值
- 一个线程修改了 volatile 修饰的变量的值时候，那么这个变量的新值会立即刷新回主内存
- 一个线程去读取 volatile 修饰的变量的值的时候，该变量在工作内存中的数据无效，需要重新到主内存中取读取最新的数据

- Java 内存模型中定义的 8 种内存与主内存之间的原子操作：**read(读取)、load(加载)、use(使用)、assign(赋值)、store(存储)、write(写入)、lock(锁定)、unlock(解锁)**

  <img src="img/内存与主内存之间的原子操作.jpg" style="zoom: 33%;" />

  > read：作用于主内存，将变量的值从主内存传输到工作内存，即主内存到工作内存
  >
  > load：作用于工作内存，将主内内存传输过来的变量值放入到工作内存变量副本中，即数据加载
  >
  > use：作用于工作内存，将工作内存遍历副本的值传递给执行引擎，每当 JVM 遇到需到这个变量的字节码指令时就会执行该操作
  >
  > assign：作用于工作内存，将从执行引擎接收到的值赋值给工作内存变量副本，每当 JVM 遇到一个给变量赋值的字节码指令时就会执行该操作
  >
  > store：作用于工作内存，将赋值完毕的工作变量的值写回主内存
  >
  > write：作用于主内存，将 store 传输过来的变量赋值给主内存中的变量
  >
  > 由于上述操作只能保证单挑指令的原子性，但对于多条指令组合的原子性，由于没有大面积加锁，所以 JVM 提供了另外两个原子指令：
  >
  > lock：作用于主内存，将一个变量标记为一个线程独占的状态，只是写的时候加锁，即只锁了写变量的过程
  >
  > unlock：作用于主内存，把一个处于锁定状态的变量释放，然后才能被其它线程占用。

<br>

volatile 的有序性，禁止指令重排

- 重排序是指编译器和处理器为了优化程序性能而对指令序列进行重新排序的一种手段，有时候会改变程序语句的先后顺序。

- 对于不存在数依赖关系的语句，可以重排序，但重排后的指令决不能改变原有的串行语义；对于存在数据依赖关系的语句，禁止重排序。

- volatile 底层是通过内存屏障实现禁止指令重排序的

- volatile 写之前的操作，都禁止重排序到 volatile 之后

  <img src="img/volatile 写操作.jpeg" style="zoom: 33%;" />

- volatile 读之后的操作，都禁止重排序到 volatile 之前

  <img src="img/volatile 读操作.jpeg" style="zoom: 33%;" />



<br>

volatile 的不保证原子性

<br>

volatile 的使用场景

- 单一赋值可以使用，但对于含复合运算的赋值操作不可以使用，例如 i++
- 状态标志，例如判断业务是否结束
- 针对读多余写的场景，volatile 结合内部锁一起使用，减少同步的开销。volatile 保证读取操作的可见性，synchronized 保证复合操作的原子性。

---

## CAS

#### CAS

CAS 全程是 compare and swap，它是一条 CPU 并发原语。它包含三个操作数，内存位置、预期原值以及更新值。在执行 CAS 操作的时候，将内存位置的值与预期原值比较：

- 如果相匹配，那么处理器会自动将该位置的值更新为新值
- 如果不匹配，处理器不做任何操作，多线程同时执行 CAS 操作时，只有一个会成功

CAS 是 JDK 提供的非阻塞原子性操作，它通过硬件保证了比较-更新的原子性。CAS 是一条 CPU 的原子指令（cmpxchg指令），不会造成所谓的数据不一致问题，Unsafe 类提供的 CAS 方法（例如 compareAndSwapXXX），在 intel 的 CPU 中(X86机器上)，底层实现使用的是汇编指令 cmpxchg 指令。执行 cmpxchg 指令的时候，会判断当前系统是否为多核系统，如果是，就给总线加锁，只有一个线程会对总线加锁成功，加锁成功之后会执行 CAS 操作，也就是说 CAS 的原子性实际上是 CPU 实现的，其实在这一点上还是有排他锁的，只是比起用 synchronized， 这里的排他时间要短的多，所以在多线程情况下性能会比较好。

CAS 并发原语体现在 JAVA 语言中就是 sun.misc.Unsafe 类中的各个方法。调用 UnSafe 类中的 CAS 方法，JVM 会帮我们实现出 CAS 汇编指令。这是一种完全依赖于硬件的功能，通过它实现了原子操作。再次强调，由于 CAS 是一种系统原语，原语属于操作系统用语范畴，是由若干条指令组成的，用于完成某个功能的一个过程，并且原语的执行必须是连续的，在执行过程中不允许被中断，也就是说 CAS 是一条 CPU 的原子指令，不会造成所谓的数据不一致问题。

<br>

#### Unsafe

Unsafe 是 CAS 的核心类，由于 Java 方法无法直接访问底层系统，需要通过本地（native）方法来访问，Unsafe 相当于一个后门，基于该类可以直接操作特定内存的数据。Unsafe 类存在于 sun.misc 包中，其内部方法操作可以像 C 的**指针**一样直接操作内存，因为 Java 中 CAS 操作的执行依赖于 Unsafe 类的方法。**注意 Unsafe 类中的所有方法都是 native 修饰的，也就是说 Unsafe 类中的方法都直接调用操作系统底层资源执行相应任务。**

```java
// 以 AtomicInteger 为例
public class AtomicInteger extends Number implements java.io.Serializable {
  private static final long serialVersionUID = 6214790243416807050L;

  // setup to use Unsafe.compareAndSwapInt for updates
  // Unsafe 类
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  // 变量 valueOffset，表示该变量值在内存中的偏移地址，因为 Unsafe 就是根据内存偏移地址获取数据的。
  private static final long valueOffset;

  static {
    try {
      valueOffset = unsafe.objectFieldOffset
        (AtomicInteger.class.getDeclaredField("value"));
    } catch (Exception ex) { throw new Error(ex); }
  }

  // 量 value 用 volatile 修饰，保证了多线程之间的内存可见性。
  private volatile int value;


  public final int getAndIncrement() {
    return unsafe.getAndAddInt(this, valueOffset, 1);
  }
 }
```

```java
// OpenJDK 源码，Unsafe.java unsafe.getAndAddInt(this, valueOffset, 1);
public final int getAndAddInt(Object o, long offset, int delta) {
  int v;
  do {
    v = this.getIntVolatile(o, offset);
  } while(!this.compareAndSwapInt(o, offset, v, v + delta));

  return v;
}
```

> 假设线程 A 和线程 B 两个线程同时执行 getAndAddInt 操作（分别跑在不同 CPU 上）：
>
> 1. AtomicInteger 里面的 value 原始值为 3，即主内存中 AtomicInteger 的 value 为 3，根据 JMM 模型，线程 A 和线程 B 各自持有一份值为 3 的 value 的副本分别到各自的工作内存。
> 2. 线程 A 通过 getIntVolatile(var1, var2) 拿到 value 值 3，这时线程 A 被挂起。
> 3. 线程 B 也通过 getIntVolatile(var1, var2) 方法获取到 value 值 3，此时刚好线程 B 没有被挂起并执行 compareAndSwapInt 方法比较内存值也为 3，成功修改内存值为 4，线程 B 打完收工，一切OK。
> 4. 这时线程 A 恢复，执行 compareAndSwapInt 方法比较，发现自己手里的值数字 3 和主内存的值数字 4 不一致，说明该值已经被其它线程抢先一步修改过了，那 A 线程本次修改失败，只能重新读取重新来一遍了。
> 5. 线程 A 重新获取 value 值，因为变量 value 被 volatile 修饰，所以其它线程对它的修改，线程 A 总是能够看到，线程 A 继续执行 compareAndSwapInt 进行比较替换，直到成功。

<br>

#### 自旋锁

自旋锁是指尝试获取锁的线程不会立即阻塞，而是采用**循环的方式**去尝试获取锁，当线程发现锁被占用时，会不断循环判断锁的状态，直到获取到锁。这样的好处是减少线程上下文切换的消耗，缺点是循环会消耗 CPU 资源。

```java
// 以 Unsafe 类的 getAndAddInt 方法为例
public final int getAndAddInt(Object o, long offset, int delta) {
  int v;
  do {
    v = this.getIntVolatile(o, offset);
  } while(!this.compareAndSwapInt(o, offset, v, v + delta));

  return v;
}
```

> getAndAddInt 方法采用的是 do-while 循环，如果 CAS 失败，会一直尝试。如果 CAS 长时间一直不成功，可能会给 CPU 带来很大的开销。

<br>

#### ABA 问题

CAS 会导致“ABA问题”。CAS 算法实现一个重要前提需要取出内存中某时刻的数据并在当下时刻比较并替换，那么在这个时间差类会导致数据的变化。比如说一个线程 one 从内存位置V 中取出 A，这时候另一个线程 two 也从内存中取出 A，并且线程 two 进行了一些操作将值变成了 B，然后线程 two 又将 V 位置的数据变成 A，这时候线程 one 进行 CAS 操作发现内存中仍然是 A，然后线程 one 操作成功。尽管线程 one 的 CAS 操作成功，但是不代表这个过程就是没有问题的。可以使用版本号时间戳原子引用 AtomicStampedReference 解决，也可以使用原子更新带有标记位的原子类 AtomicMarkableReference 解决。

---

## 原子操作

#### 基本类型原子类

- AtomicInteger、AtomicBoolean、AtomicLong
- 常用 API
  - public final int get()：获取当前的值
  - public final int getAndSet(int newValue)：获取当前的值，并设置新的值
  - public final int getAndIncrement()：获取当前的值，并自增
  - public final int getAndDecrement() ：获取当前的值，并自减
  - public final int getAndAdd(int delta) ：获取当前的值，并加上预期的值
  - boolean compareAndSet(int expect, int update)：如果输入的数值等于预期值，则以原子方式将该值设置为输入值（update）

<br>

#### 数组类型原子类

- AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray

<br>

#### 引用类似原子类

- AtomicReference

- AtomicStampedReference：携带版本号的引用类型原子类，可以解决 ABA 问题

  ```java
  AtomicStampedReference atomicStampedReference = new AtomicStampedReference(100,1);
  int stamp = atomicStampedReference.getStamp();
  
  atomicStampedReference.compareAndSet(100,101,stamp,stamp+1);
  atomicStampedReference.compareAndSet(101,100,atomicStampedReference.getStamp(),
          atomicStampedReference.getStamp()+1);
  ```

- AtomicMarkableReference：原子更新带有标记位的原子类。它的订阅就是将状态戳简化为 true 和 false。

<br>

#### 对象的属性修改原子类

- AtomicIntegerFieldUpdater：原子更新对象中int类型字段的值

- AtomicLongFieldUpdater：原子更新对象中Long类型字段的值

- AtomicReferenceFieldUpdater：原子更新引用类型字段的值

  > 使用目的：以一种线程安全的方式操作非线程安全对象内的某些字段。
  >
  > 使用要求：
  >
  > - 更新的对象属性必须使用 public volatile 修饰符。
  > - 因为对象的属性修改类型原子类都是抽象类，所以每次使用都必须使用静态方法 newUpdater() 创建一个更新器，并且需要设置想要更新的类和属性

<br>

#### 原子操作增强类原理深度解析

- DoubleAccumulator、DoubleAdder、LongAccumulator、**LongAdder**

- LongAdder 常用 API

  | 方法名              | 方法说明                                                     |
  | ------------------- | ------------------------------------------------------------ |
  | void add(long x)    | 将当前的 value 加 x                                          |
  | void increment()    | 将当前的 value 加 1                                          |
  | void decrement()    | 将当前的 value 减 1                                          |
  | long sum()          | 返回当前 value 值。在不存在并发更新 value 的情况下，sum 会返回一个精确值；在存在并发更新 value 的情况下，sum 不保证返回精确值 |
  | void reset()        | 将 value 重置为 0。可用于替代重新 new 一个 LongAdder，但此方法只可以在没有并发更新的情况下使用 |
  | long sumThenReset() | 获取当前 value 值，并重置当前 value 值为 0                   |

- LongAdder 只能用来计算加法，且从零开始计算

- LongAccumulator 提供了自定义的函数操作。构造器传参需传入一个 function 的函数操作和操作的初始值

- LongAdder 的计算性能高

  ```txt
  costTime: 1558 毫秒	 number++	 clickBySync result: 50000000
  costTime: 3731 毫秒	 atomicLong.incrementAndGet()	 clickByAtomicLong result: 50000000
  costTime: 313 毫秒	 longAdder.increment()	 clickByLongAdder result: 50000000
  costTime: 635 毫秒	 longAccumulator.accumulate(1)	 clickByLongAccumulator result: 50000000
  ```

<br>

##### LongAdder 架构

<img src="img/LongAdder架构.jpg" style="zoom: 33%;" />

> LongAdder 是 Striped64 的子类。
>
> 阿里 Java 开发手册：**对于多写多读的 count++ 操作，推荐使用 JDK 8 的 LongAdder 对象，比 AtomicLong 性能更好（减少乐观锁的重试次数）**。

<br>

##### Striped64 几个重要的成员函数

```java
// 当前计算器 CPU 数量，Cell 数组扩容时会用到
static final int NCPU = Runtime.getRuntime().availableProcessors();

// ⭐️⭐️⭐️⭐️⭐️ cells 数组，为 2 的幂，2,4,8,16.....，方便以后位运算。
transient volatile Cell[] cells;

// Cell 是 Striped64 的一个内部类
@sun.misc.Contended static final class Cell {}

/**
 * ⭐️⭐️⭐️⭐️⭐️
 * 类似于 AtomicLong 中全局的 value 值。在没有竞争情况下数据直接累加到 base 上，
 * 或者 cells 扩容时，也需要将数据写入到 base 上
 */
transient volatile long base;

/**
 * 初始化 cells 或者扩容 cells 需要获取锁
 * 0 表示无锁状态；1 表示其它线程已经持有了锁
 */
transient volatile int cellsBusy;

// 表示扩容意向；false：一定不会扩容，true：可能会扩容
boolean collide;

// 通过 CAS 操作修改 cellsBusy 的值，CAS 成功代表获取锁，返回 true
final boolean casCellsBusy();

// 获取当前线程的 hash 值
static final int getProbe();

// 重置当前线程的 hash 值
static final int advanceProbe(int probe);
```

<br>

##### LongAdder 原理

- LongAdder 的基本思路就是分散热点，将 value 值分散到一个 Cell 数组中，不同线程会命中到数组的不同槽中，各个线程对自己槽中的那个值进行 CAS 操作，这样热点就被分散了，冲突的概率就小很多。如果要获取真正的 long 值，只要将各个槽中的变量值累加返回即可。

- base 变量，在非竞态条件下，直接累加到该变量上。

- Cells[] 数组，在竞态条件下，累加各个线程自己的槽中的值，即 Cell[i]
- sum() 函数会将所有的 Cell 数组中的 value 和 base 累加作为返回值，核心思想就是将之前 AtmoicLong 中的一个 value 的更新压力分散到过个 value 中去，从而降低更新热点。

<img src="img/LongAdder原理.jpg" style="zoom: 33%;" />
$$
数学表达式：value = base + \sum_{i=0}^{n}{Cells[i]}
$$
<br>

##### LongAdder 源码

LongAdder 在无竞争的情况，跟 AtmoicLong 一样，对同一个 base 进行操作。当出现竞争关系时，则是采用化整为零的做法，从空间换时间，用一个数组 Cells[]，将一个 value 拆分进这个 Cells[] 数组中。多个线程需要同时对 value 进行操作时，可以对线程 id 进行 hash 得到的 has 值，在根据 has 值映射到这个数组的某个下标，在对该下标锁对应的值进行自增操作。但所有线程操作完毕后，将数组中的所有元素的值和无竞争值 base 累加起来作为最终结果。

```java
public void increment() {
    add(1L);
}

/**
 * as：Striped64 中 Cells 数组的引用
 * b：Striped64 中 base 的值
 * v：当前线程 hash 到的 Cell 中的存储的值
 * m：Cells 数组的长度减 1，hash 时作为掩码使用
 * a: 表示当前线程命中(hash)的 Cells 数组的单元格 Cell
 */
public void add(long x) {
    Cell[] as; long b, v; int m; Cell a;
  	/**
  	 * 首次线程 ((cs = cells) != null）一定为 false，此时执行 casBase()，以 CAS 的方式更新 base 值，且只有当 CAS 失败时，才会执行 if 条件中的代码
  	 * 条件 1：Cells 数组不能为空，说明出现过竞争，Cell[] 已创建
  	 * 条件 2：cas 操作 base 失败，说明其它线程先一步修改了 base，说明出现了竞争
  	 */
    if ((cs = cells) != null || !casBase(b = base, b + x)) {
      	// true：无竞争；false：表示竞争激烈，多个线程 hase 到同一个 Cell，可能需要扩容
        boolean uncontended = true;
        /**
         * 条件 1：Cells 数组为空，说明正在出现竞争，上面是从条件 2 过来的
         * 条件 2：应该不会出现
         * 条件 3：当前线程所在的 Cell 为空，说明当前线程还没有更新过 Cell，应该初始化一个 Cell
         * 条件 4：更新当前线程所在的 Cell 失败，说明现在竞争很激烈，多个线程 has 到了同一个 Cell，扩容
         */
        if (as == null || (m = as.length - 1) < 0 ||
            // 返回的是线程中的 threadLocalRandomProbe 字段，它是通过随机数生成的一个值，对于一个确定的线程，这个值是固定的
            (a = as[getProbe() & m]) == null ||
            !(uncontended = a.cas(v = a.value, v + x)))
          	// 调用 Striped64 中的方法
            longAccumulate(x, null, uncontended);
    }
}

final boolean casBase(long cmp, long val) {
   return UNSAFE.compareAndSwapLong(this, BASE, cmp, val);
}
```

```java
/**
 * long x：需要增加的值，一般默认是 1
 * LongBinaryOperator fn：默认传递的是 null
 * boolean wasUncontended：竞争标识，如果是 false，则代表有竞争。只有 Cells 数组初始化之后，并且当前线程 CAS 竞争修改失败，才会是 false。
 * 
 * 首先给当前线程分配一个hash值，然后进入一个for(;;)自旋，这个自旋分为三个分支：
 * 1.Cell[]数组已经初始化
 * 2.Cell[]数组未初始化(首次新建)
 * 3.Cell[]数组正在初始化中
 */
final void longAccumulate(long x, LongBinaryOperator fn,
                              boolean wasUncontended) {
  // 存储线程的 probe 值
  int h;
  // 如果 getProbe() 得到的值是 0，说明随机数未初始化
  if ((h = getProbe()) == 0) {
    // 使用 ThreadLocalRandom 为当前线程重新计算一个 hash 值，强制初始化
    ThreadLocalRandom.current(); // force initialization
    // 重新获取 probe 值，hash 值被重置就好比一个全新的线程一样
    h = getProbe();
    // 重新计算了当前线程的 hash 后，认为此次不算是一次竞争。都未初始化，可能不存在竞争激烈，即将 wasUncontended 竞争状态设置为 true
    wasUncontended = true;
  }
  boolean collide = false;                // True if last slot nonempty
  for (;;) {
    Cell[] as; Cell a; int n; long v;
    
    // CASE1：Cells 数组已经被初始化了
    if ((as = cells) != null && (n = as.length) > 0) {
      if ((a = as[(n - 1) & h]) == null) {
        // 至此，说明当前线程映射到的 Cells 数组的单元格中的 Cell 为 null，说明 Cell 没有被使用
        if (cellsBusy == 0) {       // Try to attach new Cell
          // 创建一个 Cell
          Cell r = new Cell(x);   // Optimistically create
          if (cellsBusy == 0 && casCellsBusy()) {
            boolean created = false;
            try {               // Recheck under lock
              Cell[] rs; int m, j;
              // 在有锁的情况下，再次检测一遍之前的判断。[判断当前线程 hash 后指向的数据位置的元素是否为空，若为空则将新建的 Cell 添加进去，否则继续循环]
              if ((rs = cells) != null &&
                  (m = rs.length) > 0 &&
                  rs[j = (m - 1) & h] == null) {
                // 将新建的 Cell 添加到 Cells 数组中的对应位置
                rs[j] = r;
                created = true;
              }
            } finally {
              cellsBusy = 0;
            }
            if (created)
              break;
            continue;           // Slot is now non-empty
          }
        }
        collide = false;
      }
      else if (!wasUncontended)       // CAS already known to fail
        // 至此表示前一次 CAS 更新 Cell 单元失败了，存在竞争。现将 wasUncontended 重置为 true，后面会重新计算线程的 hash 值
        wasUncontended = true;      // Continue after rehash
      else if (a.cas(v = a.value, ((fn == null) ? v + x :fn.applyAsLong(v, x)))) 
        // 说明当前线程对应的数组中有了数据，也重置过 hash 值。尝试 CAS 更新 Cell 单元格中的数据
        break;
      else if (n >= NCPU || cells != as)
        // 当 Cells 数组的大小超过了 CPU 核数后，永远不会再进行扩容，扩容标识 collide 设置为 false
        collide = false;            // At max size or stale
      else if (!collide)
        // 如果扩容意向 collide 为 false，则修改为 true，后面会重新计算当前线程的 hash 值，继续循环
        collide = true;
      else if (cellsBusy == 0 && casCellsBusy()) {
        // 当前 Cells 数组和最先赋值的 as 是同一个，代表没有被其它线程修改过。尝试加锁进行扩容。
        try {
          if (cells == as) {      // Expand table unless stale
            // 扩容后的大小变为当前大小的 2 倍，并将数据拷贝至新数组中
            Cell[] rs = new Cell[n << 1];
            for (int i = 0; i < n; ++i)
              rs[i] = as[i];
            cells = rs;
          }
        } finally {
          // 释放锁设置 cellsBusy = 0，继续循环执行
          cellsBusy = 0;
        }
        collide = false;
        continue;                   // Retry with expanded table
      }
      // 计算当前线程新的hash 值
      h = advanceProbe(h);
    }
    
    /**
     * CASE2：Cells 数组没有加锁且没有初始化，则尝试对它进行加锁，并初始化 Cells 数组。[首次新建]
     * 使用了两遍（cells == as）的判断，防止重新 new 一个 Cell 数组，避免上一个线程对应数组中的值被篡改。
     */
    else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
      boolean init = false;
      try {                           // Initialize table
        if (cells == as) {
          // 新建一个大小为 2 的 Cell 数组
          Cell[] rs = new Cell[2];
          // 找到当前线程 hash 到的数组中的位置，并创建其对应的 Cell，value 值为 x，默认为 1。
          // h & 1 类似于我们之前 HashMap 中常用到的计算散列桶 index 的算法，通常都是 hash & (table.len-1)，同 hashmap 一个意思
          rs[h & 1] = new Cell(x);
          cells = rs;
          init = true;
        }
      } finally {
        cellsBusy = 0;
      }
      if (init)
        break;
    }
    
    // CASE3：Cells 数组正在进行初始化，则尝试在基数 base 上进行累加操作
    // 多线程尝试 CAS 修改失败的线程会走到此分支
    else if (casBase(v = base, ((fn == null) ? v + x : fn.applyAsLong(v, x))))
      break;                          // Fall back on using base
  }
}
```

<img src="img/LongAdder计算流程.jpg" style="zoom: 33%;" />

<br>

##### LongAdder 在并发场景下计算 sum() 不准确的原因

- sum() 执行时，并没有限制对 base 和 Cells 的更新。所以 LongAdder 不是强一致性的，它是最终一致性的。
- 首先，最终返回的是 sum() 局部变量，初始被赋值为 base，而最终返回时，很可能 base 已经被更新了，而此时局部变量 sum 不会更新，造成不一致
- 其次，这里对 Cell 的读取也无法保证是最后一次写入的值。

> Returns the current sum. The returned value is NOT an atomic snapshot; invocation in the absence of concurrent updates returns an accurate result, but concurrent updates that occur while the sum is being calculated might not be incorporated.

<br>

##### 使用总结：

- AtomicLong：
  - 线程安全，可允许一些性能损耗，要求高精度时可使用。AtomicLong 是多线程针对单个热点值 value 进行原子操作，保证精度，有性能损耗的代价。
  - 原理：CAS + 自旋
  - 场景：低并发下的全局计算，能够保证准确性。高并发下性能急剧下降，因为 CAS 操作，每次只有一个线程成功，其它失败的线程要不停的自旋直到成功。
- LongAdder：
  - 当需要在高并发下有较好的性能表现，且对值的精度要求不高时，可以使用 LongAdder。LongAdder 是每个线程拥有自己的槽，各个线程一般只对自己槽中的那个值进行 CAS 操作，保证性能，有精度不高的代价。
  - 原理：CAS + Base + Cells 数组分散。空间换时间，并分散了热点数据。
  - 场景：高并发下的全局计算。缺点是 sum 求和后还有计算线程修改结果的话，最后结果不够精确，不是强一致性，是最终一致性。

---

## ThreadLocal

ThreadLocal 提供线程局部变量副本。这些变量与正常的变量不同，因为每一个线程在访问 ThreadLocal 实例的时候（通过 get 或 set 方法）都有**自己的、独立初始化的变量副本**。**ThreadLocal 实例通常是类中的私有静态字段，使用它的目的是希望将状态（例如用户 ID 或事物 ID）与线程关联起来。**

ThreadLocal 实现每个线程都有自己专属的本地变量副本，主要解决了让每一个线程绑定自己的值，通过使用 get() 和 set() 方法，获取默认值或将其值更改为当前线程所存储的副本的值，从而避免了线程安全问题。

常用 API







---

## Java 对象内存布局和对象头



---

## Synchronized 与锁升级



---

## AbstractQueuedSynchronizer(AQS)



---

## ReentrantLock、ReentrantReadWriteLock、StampedLock讲解



---



