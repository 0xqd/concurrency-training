defmodule Table do
  defmodule Philosopher do
    defstruct name: nil, eating: 0, thunk: 0
  end

  def start do
    chopsticks = [:chopstick1, :chopstick2, :chopstick3, :chopstick4, :chopstick5]

    table = spawn_link(Table, :manage_resources, [chopsticks])

    spawn(Dine, :dine, [%Philosopher{name: "1"}, table])
    spawn(Dine, :dine, [%Philosopher{name: "2"}, table])
    spawn(Dine, :dine, [%Philosopher{name: "3"}, table])
    spawn(Dine, :dine, [%Philosopher{name: "4"}, table])
    spawn(Dine, :dine, [%Philosopher{name: "5"}, table])

    receive do: (_ -> :ok)
  end

  def manage_resources(chopsticks, waiting \\ []) do
    if length(waiting) > 0 do
      names = for {_, phil} <- waiting, do: phil.name
      if length(chopsticks) >= 2 do
        [{pid, _} | waiting] = waiting
        [chopstick1, chopstick2 | chopsticks] = chopsticks
        send pid, {:eat, [chopstick1, chopstick2]}
      end
    end
    receive do
      {:waiting, pid, phil} ->
        manage_resources(chopsticks, [{pid, phil} | waiting])
      {:release_chopsticks, free_chopsticks, _} ->
        chopsticks = free_chopsticks ++ chopsticks
        manage_resources(chopsticks, waiting)
    end
  end
end

defmodule Dine do
  # forever looping
  def dine(phil, table) do
    send table, {:waiting, self, phil}
    receive do
      {:eat, chopsticks} ->
        phil = eat(phil, chopsticks, table)
        phil = think(phil, table)
    end
    dine(phil, table)
  end

  def eat(phil, chopsticks, table) do
    phil = %{phil | eating: phil.eating + 1}
    IO.puts "#{phil.name} is eating"
    :timer.sleep(1000)
    send table, {:release_chopsticks, chopsticks, phil}
    phil
  end

  def think(phil, _) do
    :timer.sleep(1000)
    %{phil | thunk: phil.thunk + 1}
  end
end

Table.start
